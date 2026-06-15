package com.snokonoko.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val colour: String,
    val isDefault: Boolean = false,
    val type: String = "expense" // "expense" or "income"
)
