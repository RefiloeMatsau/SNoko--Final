package com.snokonoko.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MonthlyGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: MonthlyGoal)

    @Query("SELECT * FROM monthly_goals WHERE userId = :userId AND monthYear = :monthYear LIMIT 1")
    suspend fun getGoalForMonth(userId: Int, monthYear: String): MonthlyGoal?

    @Query("SELECT * FROM monthly_goals WHERE userId = :userId ORDER BY monthYear DESC")
    fun getAllGoals(userId: Int): LiveData<List<MonthlyGoal>>

    @Delete
    suspend fun deleteGoal(goal: MonthlyGoal)

    @Query("DELETE FROM monthly_goals WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Int)
}
