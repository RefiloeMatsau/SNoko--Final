package com.snokonoko.app.data

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StreakManager(context: Context) {

    private val prefs = context.getSharedPreferences("snokonoko_prefs", Context.MODE_PRIVATE)

    fun updateLoginStreak() {
        val today = LocalDate.now().toString()
        val lastLogin = prefs.getString("last_login_date", null)
        val current = prefs.getInt("login_streak", 0)

        val newStreak = when {
            lastLogin == null -> 1
            lastLogin == today -> current
            LocalDate.parse(lastLogin).plusDays(1).toString() == today -> current + 1
            else -> 1
        }

        prefs.edit()
            .putString("last_login_date", today)
            .putInt("login_streak", newStreak)
            .apply()
    }

    fun getLoginStreak(): Int = prefs.getInt("login_streak", 1)

    fun getNoSpendStreak(transactions: List<Transaction>): Int {
        val expenseDates = transactions
            .filter { it.type == "expense" }
            .map { it.date.substring(0, 10) }
            .toSet()

        if (expenseDates.isEmpty()) return 0

        var streak = 0
        var day = LocalDate.now()
        while (!expenseDates.contains(day.toString()) && streak <= 365) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    fun getMonthsUnderBudgetStreak(transactions: List<Transaction>, goals: List<MonthlyGoal>): Int {
        if (goals.isEmpty() || transactions.isEmpty()) return 0
        val goalMap = goals.associateBy { it.monthYear }
        var streak = 0
        var month = LocalDate.now().withDayOfMonth(1).minusMonths(1)

        repeat(24) {
            val key = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val goal = goalMap[key] ?: return streak
            if (goal.maxGoal <= 0) return streak

            val spent = transactions
                .filter { it.type == "expense" && it.date.startsWith(key) }
                .sumOf { it.amount }

            if (spent <= goal.maxGoal) {
                streak++
                month = month.minusMonths(1)
            } else {
                return streak
            }
        }
        return streak
    }
}
