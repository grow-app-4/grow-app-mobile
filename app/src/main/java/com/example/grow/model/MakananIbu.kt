package com.example.grow.model

data class MakananIbu(
    val id_makanan: Int,
    val nama_makanan: String?,
    val jumlah_porsi: Int,
    val hasil_analisis: Analisis
)

data class Analisis(
    val karbohidrat: Float,
    val protein: Float
)
