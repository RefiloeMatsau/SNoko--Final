package com.snokonoko.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * type = "income" or "expense"
 * date = "YYYY-MM-DD" string format
 * startTime = "HH:MM" string format (optional)
 * endTime = "HH:MM" string format (optional)
 * photoPath = absolute file path to saved photo (optional)
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val type: String,
    val category: String,
    val description: String,
    val amount: Double,
    val date: String,
    val startTime: String? = null,  // e.g. "09:30"
    val endTime: String? = null,   // e.g. "10:45"
    val photoPath: String? = null   // e.g. "/storage/.../photo.jpg"
)
