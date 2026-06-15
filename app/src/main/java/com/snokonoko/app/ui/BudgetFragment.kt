package com.snokonoko.app.ui

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snokonoko.app.data.Budget
import com.snokonoko.app.data.Transaction
import com.snokonoko.app.databinding.FragmentBudgetBinding
import com.snokonoko.app.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private var latestTransactions: List<Transaction> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            latestTransactions = transactions ?: emptyList()
            rebuildBudgetList()
        }

        viewModel.budgets.observe(viewLifecycleOwner) { _ ->
            rebuildBudgetList()
        }

        binding.fabBudget.setOnClickListener {
            AddBudgetSheet().show(parentFragmentManager, "AddBudget")
        }
    }

    private fun rebuildBudgetList() {
        val budgets = viewModel.budgets.value ?: emptyList()
        val spending = latestTransactions.filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { e -> e.value.sumOf { it.amount } }

        binding.budgetListContainer.removeAllViews()

        if (budgets.isEmpty()) {
            binding.budgetListContainer.addView(TextView(requireContext()).apply {
                text = "No budgets yet. Tap + to add one."
                setTextColor(Color.parseColor("#474747"))
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 40, 0, 40)
            })
            return
        }

        budgets.forEach { budget ->
            binding.budgetListContainer.addView(buildBudgetCard(budget, spending[budget.category] ?: 0.0))
            val space = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 10.dp)
            }
            binding.budgetListContainer.addView(space)
        }
    }

    private fun buildBudgetCard(budget: Budget, spent: Double): View {
        val ctx = requireContext()
        val pct = ((spent / budget.limitAmount) * 100).coerceIn(0.0, 100.0).toInt()
        val isOver = spent > budget.limitAmount
        val isWarn = pct >= 80 && !isOver

        val barColor = when {
            isOver -> "#FF453A"
            isWarn -> "#FF9F0A"
            else   -> "#30D158"
        }
        val chipText = if (isOver) "Over" else "$pct%"
        val chipBg = when {
            isOver -> "#1AFF453A"
            isWarn -> "#1AFF9F0A"
            else   -> "#1A30D158"
        }
        val chipTxt = when {
            isOver -> "#FF453A"
            isWarn -> "#FF9F0A"
            else   -> "#30D158"
        }

        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dp, 16.dp, 16.dp, 16.dp)
            setBackgroundColor(Color.parseColor("#0F0F0F"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Header row
        val header = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 10.dp }
        }

        // Left: category name + amounts
        val left = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        left.addView(TextView(ctx).apply {
            text = categoryLabel(budget.category)
            setTextColor(Color.WHITE)
            textSize = 14f
        })
        left.addView(TextView(ctx).apply {
            text = "${formatZAR(spent)} of ${formatZAR(budget.limitAmount)}"
            setTextColor(Color.parseColor("#8C8C8C"))
            textSize = 11f
        })
        header.addView(left)

        // Right: chip
        header.addView(TextView(ctx).apply {
            text = chipText
            setTextColor(Color.parseColor(chipTxt))
            setBackgroundColor(Color.parseColor(chipBg))
            textSize = 11f
            setPadding(8.dp, 3.dp, 8.dp, 3.dp)
        })

        card.addView(header)

        // Progress bar
        val track = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 4.dp)
            setBackgroundColor(Color.parseColor("#222222"))
        }
        val fill = View(ctx).apply {
            val w = (pct / 100f * resources.displayMetrics.widthPixels).toInt()
            layoutParams = FrameLayout.LayoutParams(w, FrameLayout.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.parseColor(barColor))
        }
        track.addView(fill)
        card.addView(track)

        return card
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
