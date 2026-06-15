package com.snokonoko.app.data

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

private data class ChallengeTemplate(
    val id: String,
    val title: String,
    val description: String,
    val metric: String,
    val category: String = "",
    val targetValue: Double
)

class ChallengeManager(context: Context) {

    private val prefs = context.getSharedPreferences("snokonoko_prefs", Context.MODE_PRIVATE)

    private val weeklyPool = listOf(
        ChallengeTemplate("no_spend_day",    "No Spend Day",       "Have at least 1 day with no expenses this week",      "no_spend_days", targetValue = 1.0),
        ChallengeTemplate("coffee_cut",      "Coffee Cutback",     "Spend nothing on coffee this week",                   "category_cap",  "coffee",         0.0),
        ChallengeTemplate("food_budget",     "Food Budget",        "Keep food spending under R300 this week",             "category_cap",  "food",           300.0),
        ChallengeTemplate("transport_save",  "Transport Saver",    "Spend less than R150 on transport this week",         "category_cap",  "transport",      150.0),
        ChallengeTemplate("entertainment",   "Entertainment Cap",  "Spend less than R200 on entertainment this week",     "category_cap",  "entertainment",  200.0),
        ChallengeTemplate("shopping_freeze", "Shopping Freeze",    "Don't spend on shopping this week",                   "category_cap",  "shopping",       0.0),
        ChallengeTemplate("weekly_limit",    "Weekly Limit",       "Keep total weekly spending under R800",               "total_cap",     targetValue = 800.0),
        ChallengeTemplate("log_daily",       "Daily Logger",       "Log at least 5 transactions this week",               "tx_count",      targetValue = 5.0)
    )

    private val monthlyPool = listOf(
        ChallengeTemplate("monthly_saver",   "Monthly Saver",      "Net save at least R500 this month",                   "net_save",      targetValue = 500.0),
        ChallengeTemplate("tx_tracker",      "Transaction Tracker","Log at least 20 transactions this month",             "tx_count",      targetValue = 20.0),
        ChallengeTemplate("fuel_cap",        "Fuel Saver",         "Keep fuel spending under R600 this month",            "category_cap",  "fuel",           600.0),
        ChallengeTemplate("no_alcohol",      "No Alcohol",         "Spend nothing on alcohol this month",                 "category_cap",  "alcohol",        0.0),
        ChallengeTemplate("sub_check",       "Subscription Check", "Keep subscriptions under R200 this month",            "category_cap",  "subscriptions",  200.0)
    )

    // Returns the 2 weekly + 1 monthly active challenges with live progress
    fun getActiveChallenges(transactions: List<Transaction>): List<Challenge> {
        ensureWeeklySelected()
        ensureMonthlySelected()

        val weekStart = currentWeekStart()
        val weekEnd   = weekStart.plusDays(6)
        val monthStart = LocalDate.now().withDayOfMonth(1)
        val monthEnd   = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())

        val id1 = prefs.getString("chal_week_1", weeklyPool[0].id)!!
        val id2 = prefs.getString("chal_week_2", weeklyPool[1].id)!!
        val idM = prefs.getString("chal_month_1", monthlyPool[0].id)!!

        val t1 = weeklyPool.first { it.id == id1 }
        val t2 = weeklyPool.first { it.id == id2 }
        val tM = monthlyPool.first { it.id == idM }

        val weekTx  = transactionsBetween(transactions, weekStart,  weekEnd)
        val monthTx = transactionsBetween(transactions, monthStart, monthEnd)

        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val result = mutableListOf(
            buildChallenge(t1, weekTx,  "weekly",  weekStart.format(fmt),  weekEnd.format(fmt)),
            buildChallenge(t2, weekTx,  "weekly",  weekStart.format(fmt),  weekEnd.format(fmt)),
            buildChallenge(tM, monthTx, "monthly", monthStart.format(fmt), monthEnd.format(fmt))
        )
        // No-alcohol is always pinned as a monthly challenge
        if (tM.id != "no_alcohol") {
            result.add(buildChallenge(
                monthlyPool.first { it.id == "no_alcohol" },
                monthTx, "monthly", monthStart.format(fmt), monthEnd.format(fmt)
            ))
        }
        return result
    }

    private fun ensureWeeklySelected() {
        val key = weekKey()
        if (prefs.getString("chal_week_key", null) != key) {
            val week = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear())
            val i1 = week % weeklyPool.size
            val i2 = (week + 4) % weeklyPool.size
            prefs.edit()
                .putString("chal_week_key", key)
                .putString("chal_week_1", weeklyPool[i1].id)
                .putString("chal_week_2", weeklyPool[if (i2 == i1) (i2 + 1) % weeklyPool.size else i2].id)
                .apply()
        }
    }

    private fun ensureMonthlySelected() {
        val key = monthKey()
        if (prefs.getString("chal_month_key", null) != key) {
            val month = LocalDate.now().monthValue
            prefs.edit()
                .putString("chal_month_key", key)
                .putString("chal_month_1", monthlyPool[month % monthlyPool.size].id)
                .apply()
        }
    }

    private fun buildChallenge(
        t: ChallengeTemplate,
        transactions: List<Transaction>,
        type: String,
        start: String,
        end: String
    ): Challenge {
        val current = when (t.metric) {
            "category_cap"  -> transactions.filter { it.type == "expense" && it.category == t.category }.sumOf { it.amount }
            "total_cap"     -> transactions.filter { it.type == "expense" }.sumOf { it.amount }
            "no_spend_days" -> countNoSpendDays(transactions, start, end).toDouble()
            "tx_count"      -> transactions.size.toDouble()
            "net_save"      -> {
                val income  = transactions.filter { it.type == "income"  }.sumOf { it.amount }
                val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
                income - expense
            }
            else -> 0.0
        }
        return Challenge(t.id, t.title, t.description, type, t.metric, t.category, t.targetValue, current, start, end)
    }

    private fun countNoSpendDays(transactions: List<Transaction>, start: String, end: String): Int {
        val expenseDates = transactions.filter { it.type == "expense" }.map { it.date.substring(0, 10) }.toSet()
        var day   = LocalDate.parse(start)
        val endDt = LocalDate.parse(end)
        var count = 0
        while (!day.isAfter(endDt) && !day.isAfter(LocalDate.now())) {
            if (!expenseDates.contains(day.toString())) count++
            day = day.plusDays(1)
        }
        return count
    }

    private fun transactionsBetween(transactions: List<Transaction>, from: LocalDate, to: LocalDate): List<Transaction> {
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return transactions.filter {
            val d = LocalDate.parse(it.date.substring(0, 10), fmt)
            !d.isBefore(from) && !d.isAfter(to)
        }
    }

    private fun currentWeekStart(): LocalDate {
        val today = LocalDate.now()
        return today.with(DayOfWeek.MONDAY)
    }

    private fun weekKey(): String {
        val now = LocalDate.now()
        val week = now.get(WeekFields.ISO.weekOfWeekBasedYear())
        return "${now.year}-W${week.toString().padStart(2, '0')}"
    }

    private fun monthKey(): String =
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
}
