package com.example.grow.data.model

import com.google.gson.annotations.SerializedName

data class MakananResponse(
    val status: String,
    val message: String,
    val data: List<Makanan>
)

data class MakananInput(
    val id_makanan: Int,
    val jumlah_porsi: Int
)

data class Makanan(
    @SerializedName("id_makanan")
    val id_makanan: Int,

    @SerializedName("nama_makanan")
    val nama_makanan: String,

    @SerializedName("bahan_makanan")
    val bahan_makanan: String,

    @SerializedName("ukuran_porsi(gram)")
    val ukuran_porsi_gram: Float,

    @SerializedName("ukuran_porsi(umpama)")
    val ukuran_porsi_umpama: String
)