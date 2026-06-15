package com.snokonoko.app.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snokonoko.app.data.CategoryTotal
import com.snokonoko.app.databinding.FragmentCategoryTotalsBinding
import com.snokonoko.app.databinding.ItemCategoryTotalBinding
import com.snokonoko.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CategoryTotalsFragment : Fragment() {

    private var _binding: FragmentCategoryTotalsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")

    private lateinit var adapter: CategoryTotalAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryTotalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set default dates (current month)
        startDate = LocalDate.now().withDayOfMonth(1)
        endDate = LocalDate.now()
        updateDateButtons()

        // Setup RecyclerView
        adapter = CategoryTotalAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Date pickers
        binding.btnStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnEndDate.setOnClickListener { showDatePicker(false) }

        // Apply filter
        binding.btnApplyFilter.setOnClickListener { applyFilter() }

        // Initial filter
        applyFilter()
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val current = if (isStartDate) startDate ?: LocalDate.now() else endDate ?: LocalDate.now()
        val calendar = Calendar.getInstance()
        calendar.set(current.year, current.monthValue - 1, current.dayOfMonth)

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selected = LocalDate.of(year, month + 1, day)
                if (isStartDate) {
                    startDate = selected
                } else {
                    endDate = selected
                }
                updateDateButtons()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateButtons() {
        binding.btnStartDate.text = startDate?.format(displayFormatter) ?: "Select Start Date"
        binding.btnEndDate.text = endDate?.format(displayFormatter) ?: "Select End Date"
    }

    private fun applyFilter() {
        val start = startDate ?: return
        val end = endDate ?: return

        val startStr = start.format(dateFormatter)
        val endStr = end.format(dateFormatter)

        lifecycleScope.launch {
            val categoryTotals = viewModel.getCategoryTotals(startStr, endStr)
            adapter.submitList(categoryTotals)
            updateSummary(categoryTotals)
        }
    }

    private fun updateSummary(totals: List<CategoryTotal>) {
        val nf = NumberFormat.getInstance(Locale("en", "ZA"))
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2

        val total = totals.sumOf { it.total }
        binding.tvTotalAmount.text = "R ${nf.format(total)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // RecyclerView Adapter
    inner class CategoryTotalAdapter : RecyclerView.Adapter<CategoryTotalAdapter.ViewHolder>() {

        private var totals = listOf<CategoryTotal>()

        fun submitList(list: List<CategoryTotal>) {
            totals = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCategoryTotalBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(totals[position])
        }

        override fun getItemCount() = totals.size

        inner class ViewHolder(
            private val binding: ItemCategoryTotalBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(categoryTotal: CategoryTotal) {
                val nf = NumberFormat.getInstance(Locale("en", "ZA"))
                nf.minimumFractionDigits = 2
                nf.maximumFractionDigits = 2

                binding.tvCategory.text = categoryLabel(categoryTotal.category)
                binding.tvTotal.text = "R ${nf.format(categoryTotal.total)}"

                // Category icon
                val catColor = categoryColor(categoryTotal.category)
                val iconRes = categoryIcon(categoryTotal.category)
                binding.ivCategoryIcon.setImageResource(iconRes)
                binding.ivCategoryIcon.setColorFilter(Color.parseColor(catColor))

                // Icon background
                binding.iconContainer.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(catColor + "22"))
                }
            }
        }
    }
}
