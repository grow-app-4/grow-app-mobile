package com.example.grow.data.model

import com.google.gson.annotations.SerializedName

data class PertumbuhanResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Pertumbuhan>?
)

data class Pertumbuhan(
    @SerializedName("id") val id: Int,
    @SerializedName("id_anak") val idAnak: Int,
    @SerializedName("tanggal_pencatatan") val tanggalPencatatan: String,
    @SerializedName("status_stunting") val statusStunting: String
)
