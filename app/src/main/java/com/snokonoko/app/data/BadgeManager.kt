package com.snokonoko.app.data

import android.content.Context

class BadgeManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("snokonoko_prefs", Context.MODE_PRIVATE)

    val allBadges = listOf(
        Badge("first_transaction", "First Step",     "Log your first transaction",            "#30D158"),
        Badge("login_7",          "Week Warrior",    "7-day login streak",                    "#FF9F0A"),
        Badge("login_30",         "Month Legend",    "30-day login streak",                   "#FF9F0A"),
        Badge("no_spend_3",       "Saving Start",    "3 days without spending",               "#5AC8FA"),
        Badge("no_spend_7",       "Spend Nothing",   "7 days without spending",               "#0A84FF"),
        Badge("budget_1",         "Budget Kept",     "Stay under budget for 1 month",         "#BF5AF2"),
        Badge("budget_3",         "Hat-trick",       "3 months in a row under budget",        "#AF52DE"),
        Badge("tx_50",            "Half Century",    "Log 50 transactions",                   "#FF6B6B"),
        Badge("tx_100",           "Century",         "Log 100 transactions",                  "#FF453A"),
        Badge("saver_1000",       "Big Saver",       "Net save R1 000 in a single month",     "#34C759")
    )

    fun evaluate(
        transactions: List<Transaction>,
        streakManager: StreakManager,
        goals: List<MonthlyGoal>
    ): List<Badge> {
        val loginStreak  = streakManager.getLoginStreak()
        val noSpend      = streakManager.getNoSpendStreak(transactions)
        val budgetStreak = streakManager.getMonthsUnderBudgetStreak(transactions, goals)

        val earned = getEarnedIds().toMutableSet()

        if (transactions.isNotEmpty())  earned.add("first_transaction")
        if (loginStreak  >= 7)          earned.add("login_7")
        if (loginStreak  >= 30)         earned.add("login_30")
        if (noSpend      >= 3)          earned.add("no_spend_3")
        if (noSpend      >= 7)          earned.add("no_spend_7")
        if (budgetStreak >= 1)          earned.add("budget_1")
        if (budgetStreak >= 3)          earned.add("budget_3")
        if (transactions.size >= 50)    earned.add("tx_50")
        if (transactions.size >= 100)   earned.add("tx_100")
        if (checkBigSaver(transactions)) earned.add("saver_1000")

        saveEarnedIds(earned)
        return allBadges.map { it.copy(earned = it.id in earned) }
    }

    private fun checkBigSaver(transactions: List<Transaction>): Boolean =
        transactions
            .groupBy { it.date.substring(0, 7) }
            .any { (_, txs) ->
                val income  = txs.filter { it.type == "income"  }.sumOf { it.amount }
                val expense = txs.filter { it.type == "expense" }.sumOf { it.amount }
                (income - expense) >= 1000.0
            }

    fun getEarnedIds(): Set<String> =
        prefs.getStringSet("earned_badges", emptySet()) ?: emptySet()

    private fun saveEarnedIds(ids: Set<String>) =
        prefs.edit().putStringSet("earned_badges", ids).apply()
}
