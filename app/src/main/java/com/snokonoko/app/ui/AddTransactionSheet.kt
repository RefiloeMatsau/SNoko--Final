package com.snokonoko.app.ui

import android.app.AlertDialog
import android.widget.EditText
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.snokonoko.app.R
import com.snokonoko.app.data.Category
import com.snokonoko.app.data.Transaction
import com.snokonoko.app.databinding.SheetAddTransactionBinding
import com.snokonoko.app.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class AddTransactionSheet : BottomSheetDialogFragment() {

    private var _binding: SheetAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private var selectedType = "expense"
    private var allCategories: List<Category> = emptyList()

    private var currentPhotoPath: String? = null
    private var editTransaction: Transaction? = null
    private var isEditMode = false

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun newInstance(transaction: Transaction): AddTransactionSheet {
            val sheet = AddTransactionSheet()
            sheet.arguments = Bundle().apply {
                putInt("id", transaction.id)
                putString("type", transaction.type)
                putString("category", transaction.category)
                putString("description", transaction.description)
                putDouble("amount", transaction.amount)
                putString("date", transaction.date)
                putString("startTime", transaction.startTime)
                putString("photoPath", transaction.photoPath)
            }
            return sheet
        }
    }

    private var tempPhotoUri: Uri? = null

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempPhotoUri?.let { savePhotoToInternalStorage(it)?.let { path -> currentPhotoPath = path; updatePhotoDisplay() } }
    }

    private val pickPhotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { savePhotoToInternalStorage(it)?.let { path -> currentPhotoPath = path; updatePhotoDisplay() } }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    override fun getTheme(): Int = R.style.BottomSheetStyle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check for edit mode
        arguments?.let { args ->
            if (args.containsKey("id")) {
                isEditMode = true
                val userId = prefs().getInt("user_id", -1)
                editTransaction = Transaction(
                    id = args.getInt("id"),
                    userId = userId,
                    type = args.getString("type") ?: "expense",
                    category = args.getString("category") ?: "",
                    description = args.getString("description") ?: "",
                    amount = args.getDouble("amount"),
                    date = args.getString("date") ?: "",
                    startTime = args.getString("startTime"),
                    endTime = null,
                    photoPath = args.getString("photoPath")
                )
            }
        }

        // Show date (read-only) — original date in edit mode, today for new
        val displayDate = if (isEditMode && editTransaction != null) {
            editTransaction!!.date
        } else {
            LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        }
        binding.tvDateDisplay.text = displayDate

        // Observe categories
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            allCategories = categories ?: emptyList()
            updateCategorySpinner()
        }

        // Type toggle
        binding.btnExpense.setOnClickListener { selectedType = "expense"; updateToggleUI() }
        binding.btnIncome.setOnClickListener  { selectedType = "income";  updateToggleUI() }

        // Photo
        binding.btnAddPhoto.setOnClickListener { showPhotoOptionsPopup() }
        binding.btnRemovePhoto.setOnClickListener { currentPhotoPath = null; updatePhotoDisplay() }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { saveTransaction() }

        // Load edit data after setting up observers
        if (isEditMode) {
            editTransaction?.let { tx ->
                selectedType = tx.type
                binding.etDescription.setText(tx.description)
                binding.etAmount.setText(tx.amount.toString())
                currentPhotoPath = tx.photoPath
                updatePhotoDisplay()
            }
            binding.tvTitle.text = "Edit Transaction"
            binding.btnSave.text = "Update"
        }

        updateToggleUI()
    }

    private fun prefs() = requireContext().getSharedPreferences("snokonoko_prefs", 0)

    private fun updateToggleUI() {
        val pink = android.graphics.Color.parseColor("#F49AC2")
        val dim  = android.graphics.Color.parseColor("#8C8C8C")
        if (selectedType == "expense") {
            binding.btnExpense.setBackgroundColor(pink)
            binding.btnExpense.setTextColor(android.graphics.Color.WHITE)
            binding.btnIncome.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.btnIncome.setTextColor(dim)
            binding.tvTypeBadge.text = "EXPENSE"
        } else {
            binding.btnIncome.setBackgroundColor(pink)
            binding.btnIncome.setTextColor(android.graphics.Color.WHITE)
            binding.btnExpense.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.btnExpense.setTextColor(dim)
            binding.tvTypeBadge.text = "INCOME"
        }
        updateCategorySpinner()
    }

    private fun updateCategorySpinner() {
        val filtered = allCategories.filter { it.type == selectedType }
        val names = filtered.map { it.name }.toMutableList()
        names.add("+ Create New Category")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerCategory.adapter = adapter

        // In edit mode, try to pre-select the existing category
        if (isEditMode) {
            val editCat = editTransaction?.category ?: ""
            val idx = filtered.indexOfFirst { it.name == editCat }
            if (idx >= 0) binding.spinnerCategory.setSelection(idx)
        }

        binding.spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == names.size - 1) showCreateCategoryDialog()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun saveTransaction() {
        val desc = binding.etDescription.text.toString().trim()
        val amt  = binding.etAmount.text.toString().toDoubleOrNull()

        if (desc.isEmpty() || amt == null || amt <= 0) {
            Toast.makeText(requireContext(), "Please fill in description and amount.", Toast.LENGTH_SHORT).show()
            return
        }

        val filtered = allCategories.filter { it.type == selectedType }
        val catIdx = binding.spinnerCategory.selectedItemPosition
        if (filtered.isEmpty() || catIdx >= filtered.size) {
            Toast.makeText(requireContext(), "Please create a category first.", Toast.LENGTH_SHORT).show()
            return
        }
        val cat = filtered[catIdx].name

        val userId = prefs().getInt("user_id", -1)
        val now = LocalDate.now()

        val transaction = Transaction(
            id = if (isEditMode) editTransaction?.id ?: 0 else 0,
            userId = userId,
            type = selectedType,
            category = cat,
            description = desc,
            amount = amt,
            date = if (isEditMode) editTransaction?.date ?: now.format(dateFormatter) else now.format(dateFormatter),
            startTime = if (isEditMode) editTransaction?.startTime else LocalTime.now().format(timeFormatter),
            endTime = null,
            photoPath = currentPhotoPath
        )

        if (isEditMode) viewModel.updateTransaction(transaction)
        else viewModel.addTransaction(transaction)
        dismiss()
    }

    private fun showCreateCategoryDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "Category name"
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.parseColor("#8C8C8C"))
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Create New ${selectedType.replaceFirstChar { it.uppercase() }} Category")
            .setView(editText)
            .setPositiveButton("Create") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    val userId = prefs().getInt("user_id", -1)
                    viewModel.addCategory(Category(userId = userId, name = name, colour = "#8E8E93", type = selectedType))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPhotoOptionsPopup() {
        val popup = android.widget.PopupMenu(requireContext(), binding.btnAddPhoto)
        popup.menu.add("Take Photo")
        popup.menu.add("Choose from Gallery")
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Take Photo" -> checkCameraPermissionAndLaunch()
                "Choose from Gallery" -> pickPhotoLauncher.launch("image/*")
            }
            true
        }
        popup.show()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = try {
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("temp_", ".jpg", storageDir)
        } catch (e: IOException) { null } ?: return

        val photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
        tempPhotoUri = photoUri
        takePhotoLauncher.launch(photoUri)
    }

    private fun savePhotoToInternalStorage(sourceUri: Uri): String? {
        return try {
            val bitmap = requireContext().contentResolver.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it) } ?: return null
            val scaled = Bitmap.createScaledBitmap(bitmap, 800, 800 * bitmap.height / bitmap.width, true)
            val file = File(requireContext().filesDir, "receipt_${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { scaled.compress(Bitmap.CompressFormat.JPEG, 80, it) }
            file.absolutePath
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving photo", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun updatePhotoDisplay() {
        if (currentPhotoPath != null) {
            binding.ivPhotoThumbnail.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
            binding.ivPhotoThumbnail.visibility = View.VISIBLE
            binding.btnRemovePhoto.visibility = View.VISIBLE
        } else {
            binding.ivPhotoThumbnail.setImageDrawable(null)
            binding.ivPhotoThumbnail.visibility = View.GONE
            binding.btnRemovePhoto.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
