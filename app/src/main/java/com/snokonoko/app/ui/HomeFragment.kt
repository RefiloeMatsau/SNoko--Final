package com.snokonoko.app.ui

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snokonoko.app.R
import com.snokonoko.app.data.StreakManager
import com.snokonoko.app.data.Transaction
import com.snokonoko.app.databinding.FragmentHomeBinding
import com.snokonoko.app.viewmodel.MainViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var streakManager: StreakManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        streakManager = StreakManager(requireContext())

        // Load current month goal
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        viewModel.loadMonthlyGoal(currentMonth)

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            updateUI(transactions ?: emptyList())
            updateStreaks(transactions ?: emptyList())
        }

        viewModel.currentMonthGoal.observe(viewLifecycleOwner) { goal ->
            updateGoalProgress(goal)
        }

        binding.fab.setOnClickListener {
            AddTransactionSheet().show(parentFragmentManager, "AddTransaction")
        }

        binding.btnFilter.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, DateFilterFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnCategoryTotals.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CategoryTotalsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun updateStreaks(transactions: List<Transaction>) {
        binding.tvLoginStreak.text = streakManager.getLoginStreak().toString()
        binding.tvNoSpendStreak.text = streakManager.getNoSpendStreak(transactions).toString()
        val goals = viewModel.monthlyGoals.value ?: emptyList()
        binding.tvBudgetStreak.text = streakManager.getMonthsUnderBudgetStreak(transactions, goals).toString()
    }

    private fun updateGoalProgress(goal: com.snokonoko.app.data.MonthlyGoal?) {
        if (goal == null || goal.maxGoal <= 0) {
            binding.goalCard.visibility = View.GONE
            return
        }

        val transactions = viewModel.transactions.value ?: emptyList()
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val monthlyExpense = transactions
            .filter { it.type == "expense" && it.date.startsWith(currentMonth) }
            .sumOf { it.amount }

        binding.goalCard.visibility = View.VISIBLE

        val nf = NumberFormat.getInstance(Locale("en", "ZA"))
        nf.minimumFractionDigits = 0
        nf.maximumFractionDigits = 0

        binding.tvGoalProgress.text = "R ${nf.format(monthlyExpense)} of R ${nf.format(goal.maxGoal)}"

        // Calculate progress percentage
        val progressPercent = (monthlyExpense / goal.maxGoal * 100).toInt().coerceIn(0, 100)
        val layoutParams = binding.progressBarFill.layoutParams
        layoutParams.width = (binding.goalCard.width * progressPercent / 100).coerceAtLeast(1)
        binding.progressBarFill.layoutParams = layoutParams

        // Change color based on progress
        val progressColor = when {
            monthlyExpense < goal.minGoal -> Color.parseColor("#30D158") // Green - below min
            monthlyExpense > goal.maxGoal -> Color.parseColor("#FF453A") // Red - over max
            else -> Color.parseColor("#FF9F0A") // Orange - in range
        }
        binding.progressBarFill.setBackgroundColor(progressColor)
    }

    private fun updateUI(transactions: List<Transaction>) {
        val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = income - expense

        binding.tvBalance.text = formatZAR(balance)
        binding.tvBalance.setTextColor(if (balance >= 0) Color.parseColor("#30D158") else Color.parseColor("#FF453A"))
        binding.tvIncome.text = formatZAR(income)
        binding.tvExpenses.text = formatZAR(expense)
        binding.tvTxCount.text = "${transactions.size} transactions"

        // Show recent 6 transactions
        binding.txListContainer.removeAllViews()
        val recent = transactions.take(6)
        if (recent.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = "No transactions yet"
                setTextColor(Color.parseColor("#474747"))
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 40, 0, 40)
            }
            binding.txListContainer.addView(empty)
        } else {
            recent.forEachIndexed { index, tx ->
                val row = buildTransactionRow(tx)
                binding.txListContainer.addView(row)
                if (index < recent.size - 1) {
                    val divider = View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1
                        ).also { it.setMargins(16.dp, 0, 16.dp, 0) }
                        setBackgroundColor(Color.parseColor("#1A1A1A"))
                    }
                    binding.txListContainer.addView(divider)
                }
            }
        }
    }

    private fun buildTransactionRow(tx: Transaction): View {
        val ctx = requireContext()
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16.dp, 14.dp, 16.dp, 14.dp)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val catColor = categoryColor(tx.category)

        // Category icon circle with Android system icon
        val iconContainer = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(40.dp, 40.dp).also {
                it.marginEnd = 12.dp
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(catColor + "22"))
            }
        }

        val icon = ImageView(ctx).apply {
            setImageResource(categoryIcon(tx.category))
            setColorFilter(Color.parseColor(catColor))
            layoutParams = FrameLayout.LayoutParams(24.dp, 24.dp).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        iconContainer.addView(icon)
        row.addView(iconContainer)

        // Description + sub info
        val infoCol = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        infoCol.addView(TextView(ctx).apply {
            text = tx.description
            setTextColor(Color.WHITE)
            textSize = 13f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        })
        infoCol.addView(TextView(ctx).apply {
            text = "${categoryLabel(tx.category)} ${tx.date.substring(5).replace("-", " ")}"
            setTextColor(Color.parseColor("#474747"))
            textSize = 11f
        })
        row.addView(infoCol)

        // Amount
        row.addView(TextView(ctx).apply {
            val sign = if (tx.type == "income") "+" else "-"
            text = "$sign${formatZAR(tx.amount)}"
            setTextColor(if (tx.type == "income") Color.parseColor("#30D158") else Color.WHITE)
            textSize = 14f
        })

        // Long press for edit/delete options
        row.setOnLongClickListener {
            showTransactionOptions(tx)
            true
        }

        return row
    }

    private fun showTransactionOptions(tx: Transaction) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(requireContext())
            .setTitle(tx.description)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editTransaction(tx)
                    1 -> confirmDeleteTransaction(tx)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editTransaction(tx: Transaction) {
        // Launch AddTransactionSheet in edit mode
        val sheet = AddTransactionSheet.newInstance(tx)
        sheet.show(parentFragmentManager, "EditTransaction")
    }

    private fun confirmDeleteTransaction(tx: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete \"${tx.description}\"?")
            .setPositiveButton("Delete") { dialog: android.content.DialogInterface, which: Int ->
                viewModel.deleteTransaction(tx)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    private fun formatZAR(amount: Double): String {
        val nf = NumberFormat.getInstance(Locale("en", "ZA"))
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return "R ${nf.format(abs(amount))}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Shared helpers
fun categoryColor(cat: String): String = when (cat) {
    "food"          -> "#FF6B6B"
    "groceries"     -> "#FF453A"
    "coffee"        -> "#A2845E"
    "alcohol"       -> "#FF375F"
    "transport"     -> "#0A84FF"
    "fuel"          -> "#007AFF"
    "shopping"      -> "#BF5AF2"
    "clothing"      -> "#AF52DE"
    "entertainment" -> "#FF9F0A"
    "fitness"       -> "#FF375F"
    "utilities"     -> "#636366"
    "rent"          -> "#8E8E93"
    "medical"       -> "#34C759"
    "education"     -> "#5AC8FA"
    "pets"          -> "#FF9500"
    "travel"        -> "#5856D6"
    "gifts"         -> "#FF2D55"
    "subscriptions" -> "#64D2FF"
    "income"        -> "#30D158"
    else            -> "#8E8E93"
}

fun categoryLabel(cat: String): String = when (cat) {
    "food"          -> "Food & Dining"
    "groceries"     -> "Groceries"
    "coffee"        -> "Coffee & Snacks"
    "alcohol"       -> "Alcohol & Bars"
    "transport"     -> "Transport"
    "fuel"          -> "Fuel"
    "shopping"      -> "Shopping"
    "clothing"      -> "Clothing"
    "entertainment" -> "Entertainment"
    "fitness"       -> "Fitness & Gym"
    "utilities"     -> "Utilities"
    "rent"          -> "Rent & Housing"
    "medical"       -> "Medical & Health"
    "education"     -> "Education"
    "pets"          -> "Pets"
    "travel"        -> "Travel"
    "gifts"         -> "Gifts & Donations"
    "subscriptions" -> "Subscriptions"
    "income"        -> "Income"
    else            -> "Other"
}

fun categoryIcon(cat: String): Int = when (cat.lowercase()) {
    "food"          -> android.R.drawable.ic_menu_myplaces
    "groceries"     -> android.R.drawable.ic_menu_view
    "coffee"        -> android.R.drawable.ic_menu_day
    "alcohol"       -> android.R.drawable.ic_menu_more
    "transport"     -> android.R.drawable.ic_menu_directions
    "fuel"          -> android.R.drawable.ic_menu_rotate
    "shopping"      -> android.R.drawable.ic_menu_sort_by_size
    "clothing"      -> android.R.drawable.ic_menu_gallery
    "entertainment" -> android.R.drawable.ic_menu_slideshow
    "fitness"       -> android.R.drawable.ic_menu_preferences
    "utilities"     -> android.R.drawable.ic_menu_manage
    "rent"          -> android.R.drawable.ic_menu_info_details
    "medical"       -> android.R.drawable.ic_menu_info_details
    "education"     -> android.R.drawable.ic_menu_agenda
    "pets"          -> android.R.drawable.ic_menu_compass
    "travel"        -> android.R.drawable.ic_menu_mapmode
    "gifts"         -> android.R.drawable.ic_menu_send
    "subscriptions" -> android.R.drawable.ic_menu_recent_history
    "income"        -> android.R.drawable.ic_menu_add
    else            -> android.R.drawable.ic_menu_help
}

val EXPENSE_CATEGORIES = listOf(
    "food", "groceries", "coffee", "alcohol",
    "transport", "fuel", "shopping", "clothing",
    "entertainment", "fitness", "utilities", "rent",
    "medical", "education", "pets", "travel",
    "gifts", "subscriptions", "other"
)
