package com.snokonoko.app.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snokonoko.app.data.Transaction
import com.snokonoko.app.databinding.FragmentDateFilterBinding
import com.snokonoko.app.databinding.ItemTransactionWithPhotoBinding
import com.snokonoko.app.viewmodel.MainViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DateFilterFragment : Fragment() {

    private var _binding: FragmentDateFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")

    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDateFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set default dates (current month)
        startDate = LocalDate.now().withDayOfMonth(1)
        endDate = LocalDate.now()
        updateDateButtons()

        // Setup RecyclerView
        adapter = TransactionAdapter { transaction ->
            // Handle photo click
            transaction.photoPath?.let { path ->
                showPhotoViewer(path)
            }
        }
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

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val filtered = transactions?.filter {
                it.date >= startStr && it.date <= endStr
            } ?: emptyList()

            adapter.submitList(filtered)
            updateSummary(filtered)
        }
    }

    private fun updateSummary(transactions: List<Transaction>) {
        val nf = NumberFormat.getInstance(Locale("en", "ZA"))
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2

        val total = transactions.sumOf { it.amount }
        binding.tvResultCount.text = "${transactions.size} transactions"
        binding.tvTotalAmount.text = "Total: R ${nf.format(total)}"
    }

    private fun showPhotoViewer(photoPath: String) {
        // Launch photo viewer activity or dialog
        val intent = android.content.Intent(requireContext(), PhotoViewerActivity::class.java).apply {
            putExtra("photo_path", photoPath)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // RecyclerView Adapter
    inner class TransactionAdapter(
        private val onPhotoClick: (Transaction) -> Unit
    ) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

        private var transactions = listOf<Transaction>()

        fun submitList(list: List<Transaction>) {
            transactions = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemTransactionWithPhotoBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(transactions[position])
        }

        override fun getItemCount() = transactions.size

        inner class ViewHolder(
            private val binding: ItemTransactionWithPhotoBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(tx: Transaction) {
                val nf = NumberFormat.getInstance(Locale("en", "ZA"))
                nf.minimumFractionDigits = 2
                nf.maximumFractionDigits = 2

                binding.tvDescription.text = tx.description
                binding.tvCategoryDate.text = "${tx.category} • ${tx.date}"
                binding.tvAmount.text = "R ${nf.format(tx.amount)}"
                binding.tvAmount.setTextColor(
                    if (tx.type == "income") Color.parseColor("#30D158") else Color.WHITE
                )

                // Category icon
                val catColor = categoryColor(tx.category)
                val iconRes = categoryIcon(tx.category)
                binding.ivCategoryIcon.setImageResource(iconRes)
                binding.ivCategoryIcon.setColorFilter(Color.parseColor(catColor))

                // Icon background
                binding.iconContainer.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(catColor + "22"))
                }

                // Photo indicator
                if (tx.photoPath != null) {
                    binding.ivPhotoIndicator.visibility = View.VISIBLE
                    binding.ivPhotoIndicator.setOnClickListener { onPhotoClick(tx) }
                } else {
                    binding.ivPhotoIndicator.visibility = View.GONE
                }
            }
        }
    }
}
