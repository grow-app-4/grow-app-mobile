package com.example.grow.model

data class StandarNutrisiResponse(
    val karbohidrat: Float,
    val protein: Float
    // Tambahkan nutrisi lain jika ada
)

data class StandarNutrisi(
    val id_nutrisi: Int,
    val nilai_min: Float
)