package com.snokonoko.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_goals")
data class MonthlyGoal(
    @PrimaryKey
    val userId: Int,
    val monthYear: String, // Format: "2024-04" for April 2024
    val minGoal: Double,
    val maxGoal: Double
)
