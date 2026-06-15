package com.snokonoko.app.ui

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.snokonoko.app.databinding.FragmentAnalyticsBinding
import com.snokonoko.app.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPieChart()
        setupBarChart()

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            updateCharts(transactions ?: emptyList())
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

    private fun updateCharts(transactions: List<com.snokonoko.app.data.Transaction>) {
        updatePieChart(transactions)
        updateBarChart(transactions)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
