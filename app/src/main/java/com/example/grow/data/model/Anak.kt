package com.example.grow.data.model

import com.example.grow.data.AnakEntity
import com.google.gson.annotations.SerializedName
import java.util.Date

data class AnakResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Anak>
)

data class AnakSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Anak
)

data class AnakRequest(
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("nama_anak") val namaAnak: String,
    @SerializedName("jenis_kelamin") val jenisKelamin: String,
    @SerializedName("tanggal_lahir") val tanggalLahir: String,
    @SerializedName("profile_photo") val profilePhoto: String? = null
)

data class Anak(
    @SerializedName("id_anak") val idAnak: Int,
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("nama_anak") val namaAnak: String,
    @SerializedName("jenis_kelamin") val jenisKelamin: String,
    @SerializedName("tanggal_lahir") val tanggalLahir: String,
    @SerializedName("profile_photo") val profileImageUri: String? = null
)