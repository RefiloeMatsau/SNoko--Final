package com.snokonoko.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.snokonoko.app.data.Budget
import com.snokonoko.app.data.Category
import com.snokonoko.app.data.CategoryTotal
import com.snokonoko.app.data.MonthlyGoal
import com.snokonoko.app.data.Transaction
import com.snokonoko.app.repository.FinanceRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = FinanceRepository(application)

    // Observe these in your fragments - they update automatically
    val transactions = repo.allTransactions()
    val budgets = repo.allBudgets()
    val categories = repo.allCategories()
    val monthlyGoals = repo.allMonthlyGoals()

    // Monthly goal for current month
    private val _currentMonthGoal = MutableLiveData<MonthlyGoal?>()
    val currentMonthGoal: LiveData<MonthlyGoal?> = _currentMonthGoal

    fun addTransaction(t: Transaction) = viewModelScope.launch { repo.insertTransaction(t) }
    fun updateTransaction(t: Transaction) = viewModelScope.launch { repo.updateTransaction(t) }
    fun deleteTransaction(t: Transaction) = viewModelScope.launch { repo.deleteTransaction(t) }

    fun addBudget(b: Budget) = viewModelScope.launch { repo.insertBudget(b) }
    fun updateBudget(b: Budget) = viewModelScope.launch { repo.updateBudget(b) }
    fun deleteBudget(b: Budget) = viewModelScope.launch { repo.deleteBudget(b) }

    fun addCategory(c: Category) = viewModelScope.launch { repo.insertCategory(c) }
    fun deleteCategory(c: Category) = viewModelScope.launch { repo.deleteCategory(c) }

    fun setMonthlyGoal(goal: MonthlyGoal) = viewModelScope.launch { repo.insertMonthlyGoal(goal) }

    fun clearAllTransactions() = viewModelScope.launch { repo.clearAllTransactions() }

    fun resetAllData() = viewModelScope.launch { repo.resetAllData() }

    fun loadMonthlyGoal(monthYear: String) = viewModelScope.launch {
        val goal = repo.getMonthlyGoal(monthYear)
        _currentMonthGoal.postValue(goal)
    }

    suspend fun getCategoryTotals(startDate: String, endDate: String): List<CategoryTotal> {
        return repo.getCategoryTotalsBetweenDates(startDate, endDate)
    }

    suspend fun getAllUsers() = repo.getAllUsers()

    suspend fun updateUser(id: Int, firstName: String, surname: String) = repo.updateUser(id, firstName, surname)
}
