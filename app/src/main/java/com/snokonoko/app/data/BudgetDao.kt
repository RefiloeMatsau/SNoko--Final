package com.snokonoko.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY category ASC")
    fun getAllBudgets(userId: Int): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND category = :category LIMIT 1")
    suspend fun getBudgetByCategory(userId: Int, category: String): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget)

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Int)
}
