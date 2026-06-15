package com.snokonoko.app.data

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val type: String,        // "weekly" or "monthly"
    val metric: String,      // "category_cap", "total_cap", "no_spend_days", "tx_count", "net_save"
    val category: String,    // category key for category_cap, empty otherwise
    val targetValue: Double,
    val currentValue: Double,
    val startDate: String,
    val endDate: String
) {
    val isComplete: Boolean get() = when (metric) {
        "category_cap", "total_cap" -> currentValue <= targetValue
        else                        -> currentValue >= targetValue
    }

    val progress: Float get() = when (metric) {
        "category_cap", "total_cap" ->
            if (targetValue == 0.0) (if (currentValue == 0.0) 1f else 0f)
            else (currentValue / targetValue).toFloat().coerceIn(0f, 1f)
        else ->
            if (targetValue == 0.0) 1f
            else (currentValue / targetValue).toFloat().coerceIn(0f, 1f)
    }
}
