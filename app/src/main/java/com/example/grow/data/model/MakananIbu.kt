package com.example.grow.data.model

data class MakananIbu(
    val id_makanan: Int,
    val nama_makanan: String?,
    val jumlah_porsi_dikonsumsi: Int,
    val hasil_analisis: Analisis,
    val tanggal_konsumsi: String
)

data class CheckAsupanResponse(
    val status: Boolean,
    val data: List<MakananIbu>
)

data class Analisis(
    val karbohidrat: Float,
    val protein: Float
)