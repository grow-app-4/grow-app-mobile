package com.example.grow.data.model

import com.google.gson.annotations.SerializedName

data class PertumbuhanResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Pertumbuhan>
)

data class PertumbuhanSingleResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Pertumbuhan
)

data class Pertumbuhan(
    @SerializedName("id_pertumbuhan") val idPertumbuhan: Int,
    @SerializedName("id_anak") val idAnak: Int,
    @SerializedName("tanggal_pencatatan") val tanggalPencatatan: String,
    @SerializedName("status_stunting") val statusStunting: String,
    @SerializedName("detail_pertumbuhan") val detailPertumbuhan: List<DetailPertumbuhanResponse>
)

data class DetailPertumbuhanResponse(
    @SerializedName("id_pertumbuhan") val idPertumbuhan: Int,
    @SerializedName("id_jenis") val idJenis: Int,
    @SerializedName("nilai") val nilai: Float,
    @SerializedName("jenis") val jenis: JenisPertumbuhanResponse
)

data class JenisPertumbuhanListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<JenisPertumbuhanResponse>
)

data class JenisPertumbuhanResponse(
    @SerializedName("id_jenis") val idJenis: Int,
    @SerializedName("nama_jenis") val namaJenis: String
)

data class PertumbuhanRequest(
    @SerializedName("id_anak") val idAnak: Int,
    @SerializedName("tanggal_pencatatan") val tanggalPencatatan: String,
    @SerializedName("status_stunting") val statusStunting: String,
    @SerializedName("details") val details: List<DetailRequest>
)

data class DetailRequest(
    @SerializedName("id_jenis") val idJenis: Int,
    @SerializedName("nilai") val nilai: Float
)

data class StandarPertumbuhanResponse(
    val status: String,
    val message: String,
    val data: List<StandarPertumbuhanDto>
)

data class StandarPertumbuhanDto(
    val id_jenis: Int,
    val usia: Int,
    val jenis_kelamin: String,
    val z_score: Float,
    val nilai: Float
)
