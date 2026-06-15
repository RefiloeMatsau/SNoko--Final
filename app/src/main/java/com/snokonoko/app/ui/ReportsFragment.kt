package com.snokonoko.app.ui

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.snokonoko.app.databinding.FragmentReportsBinding
import com.snokonoko.app.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPieChart()
        setupBarChart()

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val list = transactions ?: emptyList()
            val income = list.filter { it.type == "income" }.sumOf { it.amount }
            val expense = list.filter { it.type == "expense" }.sumOf { it.amount }

            binding.tvTotalIncome.text = formatZAR(income)
            binding.tvTotalSpent.text = formatZAR(expense)

            // Group expenses by category
            val byCategory = list.filter { it.type == "expense" }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .entries.sortedByDescending { it.value }

            val maxVal = byCategory.firstOrNull()?.value ?: 1.0

            binding.categoryContainer.removeAllViews()
            byCategory.forEach { (cat, amount) ->
                binding.categoryContainer.addView(buildCategoryRow(cat, amount, maxVal))
            }

            // Update charts
            updatePieChart(list)
            updateBarChart(list)
        }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            setUsePercentValues(false)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textColor = Color.WHITE
            }
        }
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            legend.isEnabled = false
            xAxis.apply {
                textColor = Color.WHITE
                setDrawGridLines(false)
            }
            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(true)
                gridColor = Color.parseColor("#33FFFFFF")
            }
            axisRight.isEnabled = false
        }
    }

    private fun updatePieChart(transactions: List<com.snokonoko.app.data.Transaction>) {
        val expensesByCategory = transactions
            .filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(6)

        val colors = listOf(
            Color.parseColor("#FF453A"),
            Color.parseColor("#FF9F0A"),
            Color.parseColor("#30D158"),
            Color.parseColor("#0A84FF"),
            Color.parseColor("#BF5AF2"),
            Color.parseColor("#64D2FF")
        )

        val entries = expensesByCategory.mapIndexed { index, (category, amount) ->
            PieEntry(amount.toFloat(), categoryLabel(category))
        }

        val dataSet = PieDataSet(entries, "").apply {
            sliceSpace = 3f
            selectionShift = 5f
            this.colors = colors.take(entries.size)
        }

        binding.pieChart.data = PieData(dataSet).apply {
            setValueTextColor(Color.WHITE)
            setValueTextSize(11f)
        }
        binding.pieChart.invalidate()
    }

    private fun updateBarChart(transactions: List<com.snokonoko.app.data.Transaction>) {
        val income = transactions.filter { it.type == "income" }.sumOf { it.amount }.toFloat()
        val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }.toFloat()

        val entries = listOf(
            BarEntry(0f, income),
            BarEntry(1f, expense)
        )

        val dataSet = BarDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#30D158"), Color.parseColor("#FF453A"))
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }

        binding.barChart.data = BarData(dataSet).apply {
            barWidth = 0.5f
        }

        binding.barChart.xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value == 0f) "Income" else "Expense"
            }
        }

        binding.barChart.invalidate()
    }

    private fun buildCategoryRow(cat: String, amount: Double, maxVal: Double): View {
        val ctx = requireContext()
        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 16.dp }
        }

        val catColor = categoryColor(cat)

        // Label row
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 6.dp }
        }
        row.addView(TextView(ctx).apply {
            text = categoryLabel(cat)
            setTextColor(Color.WHITE)
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        row.addView(TextView(ctx).apply {
            text = formatZAR(amount)
            setTextColor(Color.WHITE)
            textSize = 13f
        })
        col.addView(row)

        // Progress bar
        val track = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 4.dp)
            setBackgroundColor(Color.parseColor("#222222"))
        }
        val fill = View(ctx).apply {
            val pct = (amount / maxVal).coerceIn(0.0, 1.0)
            layoutParams = FrameLayout.LayoutParams((pct * resources.displayMetrics.widthPixels).toInt(), FrameLayout.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.parseColor(catColor))
        }
        track.addView(fill)
        col.addView(track)

        return col
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    private fun formatZAR(amount: Double): String {
        val nf = NumberFormat.getInstance(Locale("en", "ZA"))
        nf.minimumFractionDigits = 0
        nf.maximumFractionDigits = 0
        return "R ${nf.format(abs(amount))}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
