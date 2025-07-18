package com.example.grow.data.model

data class KehamilanResponse(
    val status: String,
    val message: String,
    val data: KehamilanData
)

data class UsiaKehamilanResponse(
    val bulan: Int,
    val hari: Int,
    val total_hari: Double,
    val tanggal_mulai: String
)


data class KehamilanRequest(
    val id_user: Int,
    val tanggal_mulai: String,  // format "YYYY-MM-DD"
    val berat_awal: Float
)

data class KehamilanData(
    val id: Int,
    val id_user: Int,
    val tanggal_mulai: String,
    val berat_awal: Float,
    val status: String
)