package com.example.grow.model

import com.google.gson.annotations.SerializedName

data class MakananResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Makanan>
)

data class Makanan(
    @SerializedName("id_makanan") val idMakanan: Int,
    @SerializedName("nama_makanan") val namaMakanan: String,
    @SerializedName("kategori") val kategori: String,
    @SerializedName("ukuran_porsi") val ukuranPorsi: Int
)

