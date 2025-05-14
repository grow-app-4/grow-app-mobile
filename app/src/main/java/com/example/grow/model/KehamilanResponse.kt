package com.example.grow.model

data class KehamilanResponse(
    val status: String,
    val message: String,
    val data: KehamilanData
)

data class KehamilanData(
    val id: Int,
    val id_user: Int,
    val tanggal_mulai: String,
    val berat_awal: Float,
    val status: String
)

