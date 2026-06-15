package com.snokonoko.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, id DESC")
    fun getAllTransactions(userId: Int): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, id DESC")
    suspend fun getAllTransactionsList(userId: Int): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, id DESC")
    fun getTransactionsBetweenDates(userId: Int, startDate: String, endDate: String): LiveData<List<Transaction>>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE userId = :userId AND type = 'expense' AND date BETWEEN :startDate AND :endDate GROUP BY category")
    suspend fun getCategoryTotalsBetweenDates(userId: Int, startDate: String, endDate: String): List<CategoryTotal>

    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Int)
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
