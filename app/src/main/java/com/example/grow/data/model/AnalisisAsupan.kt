package com.example.grow.data.model

data class AnalisisAsupanRequest(
    val id_user: Int,
    val makanan: List<MakananInput>
)

data class AnalisisAsupanResponse(
    val usia_kehamilan_minggu: Float,
    val rentang: String,
    val total_nutrisi: Map<String, Float>, // atau Map<Int, Float> jika pakai ID
    val hasil_analisis: Map<String, String> // karbohidrat: cukup / kurang
)