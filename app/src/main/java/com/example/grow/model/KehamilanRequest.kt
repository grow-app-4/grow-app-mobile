package com.example.grow.model

data class KehamilanRequest(
    val id_user: Int,
    val tanggal_mulai: String,  // format "YYYY-MM-DD"
    val berat_awal: Float
)
