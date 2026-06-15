package com.snokonoko.app.ui

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.snokonoko.app.R
import com.snokonoko.app.data.Budget
import com.snokonoko.app.databinding.SheetAddBudgetBinding
import com.snokonoko.app.viewmodel.MainViewModel

class AddBudgetSheet : BottomSheetDialogFragment() {

    private var _binding: SheetAddBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun getTheme(): Int = R.style.BottomSheetStyle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetAddBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val catLabels = EXPENSE_CATEGORIES.map { categoryLabel(it) }
        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, catLabels
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSave.setOnClickListener {
            val catIdx = binding.spinnerCategory.selectedItemPosition
            val cat = EXPENSE_CATEGORIES[catIdx]
            val limit = binding.etLimit.text.toString().toDoubleOrNull()

            if (limit == null || limit <= 0) {
                android.widget.Toast.makeText(requireContext(), "Please enter a valid limit.", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = requireContext().getSharedPreferences("snokonoko_prefs", 0)
            val userId = prefs.getInt("user_id", -1)
            viewModel.addBudget(Budget(userId = userId, category = cat, limitAmount = limit))
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
