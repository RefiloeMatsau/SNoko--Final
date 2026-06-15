package com.snokonoko.app.ui

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.snokonoko.app.data.MonthlyGoal
import kotlinx.coroutines.launch
import com.snokonoko.app.databinding.FragmentAccountBinding
import com.snokonoko.app.viewmodel.MainViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as com.snokonoko.app.MainActivity
        binding.tvUserName.text = activity.userName
        binding.tvUserEmail.text = activity.userEmail

        // Make profile section clickable for editing
        binding.userProfileContainer.setOnClickListener {
            showEditProfileDialog(activity.userName ?: "", activity.userEmail ?: "")
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val list = transactions ?: emptyList()
            val income = list.filter { it.type == "income" }.sumOf { it.amount }
            val expense = list.filter { it.type == "expense" }.sumOf { it.amount }
            val net = income - expense

            val nf = NumberFormat.getInstance(Locale("en", "ZA"))
            nf.minimumFractionDigits = 0
            nf.maximumFractionDigits = 0
            binding.tvNetBalance.text = "R ${nf.format(abs(net))}"
            binding.tvNetBalance.setTextColor(
                if (net >= 0) Color.parseColor("#30D158") else Color.parseColor("#FF453A")
            )
        }

        binding.btnChallenges.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.snokonoko.app.R.id.fragmentContainer, ChallengesFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnBadges.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.snokonoko.app.R.id.fragmentContainer, BadgesFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnManageCategories.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.snokonoko.app.R.id.fragmentContainer, CategoriesFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnSetMonthlyGoals.setOnClickListener {
            showMonthlyGoalDialog()
        }

        binding.btnClearData.setOnClickListener {
            showClearDataDialog()
        }

        binding.btnLogout.setOnClickListener {
            // Clear session
            requireContext().getSharedPreferences("snokonoko_prefs", 0)
                .edit().clear().apply()

            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    private fun showMonthlyGoalDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

        val etMinGoal = EditText(context).apply {
            hint = "Minimum goal (R)"
            inputType = android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val etMaxGoal = EditText(context).apply {
            hint = "Maximum goal (R)"
            inputType = android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        layout.addView(etMinGoal)
        layout.addView(etMaxGoal)

        // Load existing goal
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        viewModel.loadMonthlyGoal(currentMonth)
        viewModel.currentMonthGoal.observe(viewLifecycleOwner) { goal ->
            goal?.let {
                etMinGoal.setText(it.minGoal.toString())
                etMaxGoal.setText(it.maxGoal.toString())
            }
        }

        AlertDialog.Builder(context)
            .setTitle("Set Monthly Spending Goals")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val minStr = etMinGoal.text.toString()
                val maxStr = etMaxGoal.text.toString()
                val minGoal = minStr.toDoubleOrNull() ?: 0.0
                val maxGoal = maxStr.toDoubleOrNull() ?: 0.0

                val prefs = requireContext().getSharedPreferences("snokonoko_prefs", 0)
                val userId = prefs.getInt("user_id", -1)
                val goal = MonthlyGoal(
                    userId = userId,
                    monthYear = currentMonth,
                    minGoal = minGoal,
                    maxGoal = maxGoal
                )
                viewModel.setMonthlyGoal(goal)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset All Data")
            .setMessage("This will permanently delete all your transactions, budgets, categories, goals, and achievements. The app will return to a blank state. This cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                val prefs = requireContext().getSharedPreferences("snokonoko_prefs", 0)
                // Preserve session so the user stays logged in
                val userId = prefs.getInt("user_id", -1)
                val userName = prefs.getString("user_name", null)
                val userEmail = prefs.getString("user_email", null)

                // Wipe all prefs then restore session keys
                prefs.edit().clear().apply()
                prefs.edit()
                    .putInt("user_id", userId)
                    .apply {
                        if (userName != null) putString("user_name", userName)
                        if (userEmail != null) putString("user_email", userEmail)
                    }
                    .apply()

                // Wipe all DB tables for this user and re-seed default categories
                viewModel.resetAllData()

                // Delete saved receipt photos
                requireContext().filesDir
                    .listFiles { _, name -> name.startsWith("receipt_") }
                    ?.forEach { it.delete() }

                android.widget.Toast.makeText(requireContext(), "App data has been reset", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditProfileDialog(currentName: String, currentEmail: String) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

        val etFirstName = EditText(requireContext()).apply {
            hint = "First Name"
            val nameParts = currentName.split(" ")
            setText(if (nameParts.isNotEmpty()) nameParts[0] else "")
        }

        val etSurname = EditText(requireContext()).apply {
            hint = "Surname"
            val nameParts = currentName.split(" ")
            setText(if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else "")
        }

        val tvEmail = TextView(requireContext()).apply {
            text = "Email: $currentEmail (read-only)"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#8C8C8C"))
        }

        layout.addView(etFirstName)
        layout.addView(etSurname)
        layout.addView(tvEmail)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val firstName = etFirstName.text.toString().trim()
                val surname = etSurname.text.toString().trim()

                if (firstName.isEmpty() || surname.isEmpty()) {
                    android.widget.Toast.makeText(requireContext(), "Please fill in both fields", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val prefs = requireContext().getSharedPreferences("snokonoko_prefs", 0)
                val userId = prefs.getInt("user_id", -1)

                lifecycleScope.launch {
                    viewModel.updateUser(userId, firstName, surname)

                    // Update SharedPreferences
                    prefs.edit()
                        .putString("user_name", "$firstName $surname")
                        .apply()

                    // Update UI
                    binding.tvUserName.text = "$firstName $surname"

                    // Update MainActivity properties
                    val activity = requireActivity() as com.snokonoko.app.MainActivity
                    activity.userName = "$firstName $surname"

                    android.widget.Toast.makeText(requireContext(), "Profile updated", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
