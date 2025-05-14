package com.example.grow.model

data class NutrisiAnalisisResponse(
    val usia_kehamilan_minggu: Float,
    val rentang: String,
    val total_nutrisi: Map<String, Float>, // atau Map<Int, Float> jika pakai ID
    val hasil_analisis: Map<String, String> // karbohidrat: cukup / kurang
)

