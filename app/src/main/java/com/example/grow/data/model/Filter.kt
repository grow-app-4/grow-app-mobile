package com.example.grow.data.model

// Class untuk representasi kategori filter
enum class FilterCategory {
    TIME, RATE, CATEGORY
}

// Class untuk representasi opsi filter
data class FilterOption(
    val id: String,
    val displayText: String
)

// Class untuk menyimpan filter yang diterapkan
data class AppliedFilter(
    val id: String,
    val value: String,
    val displayText: String
)