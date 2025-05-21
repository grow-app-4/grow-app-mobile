package com.example.grow.model

import com.google.gson.annotations.SerializedName

data class AsupanAsi(
    @SerializedName("id_anak")
    val idAnak: Int,

    @SerializedName("tanggal_konsumsi")
    val tanggalKonsumsi: String,

    @SerializedName("jumlah_porsi_dikonsumsi")
    val jumlahPorsiDikonsumsi: Int,

    @SerializedName("hasil_analisis")
    val hasilAnalisis: String? = null,

    @SerializedName("usia_anak")
    val usiaAnak: String = "" // contoh: "0 bulan 27 hari"
)

data class AnalisisData(
    val jumlahPorsiDikonsumsi: Int,
    val standarFrekuensi: Int,
    val statusFrekuensi: String
)