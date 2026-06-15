package com.snokonoko.app.data

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val color: String,
    val earned: Boolean = false
)
