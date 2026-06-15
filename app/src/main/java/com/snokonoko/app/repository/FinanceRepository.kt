package com.snokonoko.app.repository

import android.content.Context
import com.snokonoko.app.data.AppDatabase
import com.snokonoko.app.data.Budget
import com.snokonoko.app.data.Category
import com.snokonoko.app.data.MonthlyGoal
import com.snokonoko.app.data.Transaction
import com.snokonoko.app.data.User

class FinanceRepository(context: Context) {

    companion object {
        val DEFAULT_EXPENSE_CATEGORIES = mapOf(
            "food"          to "#FF6B6B",
            "groceries"     to "#FF453A",
            "coffee"        to "#A2845E",
            "alcohol"       to "#FF375F",
            "transport"     to "#0A84FF",
            "fuel"          to "#007AFF",
            "shopping"      to "#BF5AF2",
            "clothing"      to "#AF52DE",
            "entertainment" to "#FF9F0A",
            "fitness"       to "#FF375F",
            "utilities"     to "#636366",
            "rent"          to "#8E8E93",
            "medical"       to "#34C759",
            "education"     to "#5AC8FA",
            "pets"          to "#FF9500",
            "travel"        to "#5856D6",
            "gifts"         to "#FF2D55",
            "subscriptions" to "#64D2FF"
        )
        val DEFAULT_INCOME_CATEGORIES = mapOf(
            "salary"     to "#30D158",
            "freelance"  to "#34C759",
            "business"   to "#5AC8FA",
            "investment" to "#0A84FF",
            "gift"       to "#FF9F0A",
            "other"      to "#8E8E93"
        )
        // Keep old name for any callers
        val DEFAULT_CATEGORIES = DEFAULT_EXPENSE_CATEGORIES
    }

    private val db = AppDatabase.getDatabase(context)
    private val txDao = db.transactionDao()
    private val budgetDao = db.budgetDao()
    private val categoryDao = db.categoryDao()
    private val monthlyGoalDao = db.monthlyGoalDao()
    private val userDao = db.userDao()

    private val prefs = context.getSharedPreferences("snokonoko_prefs", Context.MODE_PRIVATE)
    private val currentUserId: Int
        get() = prefs.getInt("user_id", 0)

    fun allTransactions() = txDao.getAllTransactions(currentUserId)
    fun allBudgets() = budgetDao.getAllBudgets(currentUserId)
    fun allCategories() = categoryDao.getAllCategories(currentUserId)
    fun allMonthlyGoals() = monthlyGoalDao.getAllGoals(currentUserId)

    suspend fun insertTransaction(t: Transaction) = txDao.insert(t.copy(userId = currentUserId))
    suspend fun updateTransaction(t: Transaction) = txDao.update(t)
    suspend fun deleteTransaction(t: Transaction) = txDao.delete(t)
    suspend fun clearAllTransactions() = txDao.deleteAllForUser(currentUserId)

    suspend fun resetAllData() {
        txDao.deleteAllForUser(currentUserId)
        budgetDao.deleteAllForUser(currentUserId)
        categoryDao.deleteAllForUser(currentUserId)
        monthlyGoalDao.deleteAllForUser(currentUserId)
        seedDefaultCategories()
    }

    suspend fun insertBudget(b: Budget) = budgetDao.insert(b.copy(userId = currentUserId))
    suspend fun updateBudget(b: Budget) = budgetDao.update(b)
    suspend fun deleteBudget(b: Budget) = budgetDao.delete(b)
    suspend fun getBudgetByCategory(cat: String) = budgetDao.getBudgetByCategory(currentUserId, cat)

    suspend fun insertCategory(c: Category) = categoryDao.insertCategory(c.copy(userId = currentUserId))
    suspend fun deleteCategory(c: Category) = categoryDao.deleteCategory(c)

    suspend fun insertMonthlyGoal(goal: MonthlyGoal) = monthlyGoalDao.insertGoal(goal.copy(userId = currentUserId))
    suspend fun getMonthlyGoal(monthYear: String) = monthlyGoalDao.getGoalForMonth(currentUserId, monthYear)

    suspend fun getCategoryTotalsBetweenDates(startDate: String, endDate: String) =
        txDao.getCategoryTotalsBetweenDates(currentUserId, startDate, endDate)

    suspend fun getAllUsers() = userDao.getAllUsers()
    suspend fun updateUser(id: Int, firstName: String, surname: String) = userDao.updateUser(id, firstName, surname)

    // Admin methods
    suspend fun adminGetAllUsers(): List<User> = userDao.getAllUsers()
    suspend fun adminGetUserTransactions(userId: Int): List<Transaction> = txDao.getAllTransactionsList(userId)
    suspend fun adminInsertUser(user: User): Long = userDao.insertUser(user)
    suspend fun adminUpdateUser(id: Int, firstName: String, surname: String, email: String, password: String) =
        userDao.updateUserAdmin(id, firstName, surname, email, password)
    suspend fun adminDeleteUser(user: User) {
        txDao.deleteAllForUser(user.id)
        userDao.deleteUser(user)
    }

    suspend fun seedDefaultCategories() {
        val existing = categoryDao.getAllCategoriesList(currentUserId)
        if (existing.none { it.type == "expense" }) {
            DEFAULT_EXPENSE_CATEGORIES.forEach { (name, color) ->
                categoryDao.insertCategory(Category(userId = currentUserId, name = name, colour = color, isDefault = true, type = "expense"))
            }
        }
        if (existing.none { it.type == "income" }) {
            DEFAULT_INCOME_CATEGORIES.forEach { (name, color) ->
                categoryDao.insertCategory(Category(userId = currentUserId, name = name, colour = color, isDefault = true, type = "income"))
            }
        }
    }
}
