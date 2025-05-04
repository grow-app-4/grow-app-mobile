package com.example.grow.model

import com.google.gson.annotations.SerializedName

data class AnakResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Anak>
)

data class Anak(
    @SerializedName("id_anak") val idAnak: Int,
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("nama_anak") val namaAnak: String,
    @SerializedName("jenis_kelamin") val jenisKelamin: String,
    @SerializedName("tanggal_lahir") val tanggalLahir: String
)