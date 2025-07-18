package com.example.grow.data.model

import com.google.gson.annotations.SerializedName

data class CatatanKehamilan(
    val id_kehamilan: Int,
    val berat: Float,
    val tanggal: String
)

data class KehamilanWithCatatan(
    val id_kehamilan: Int,
    val tanggal_mulai: String,
    val berat_awal: Float,
    val status: String,
    @SerializedName("catatan_berat")
    val catatan_kehamilan: List<CatatanKehamilan>? = null
)

data class ApiResponse<T>(
    val status: String,
    val data: T
)

data class StatusUpdateRequest(
    val status: String
)