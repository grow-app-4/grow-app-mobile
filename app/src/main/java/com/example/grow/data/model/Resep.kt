package com.example.grow.data.model

import androidx.annotation.DrawableRes
import com.google.gson.annotations.SerializedName

// Model untuk Resep
data class Resep(
    @SerializedName("id_resep") val idResep: String,
    @SerializedName("nama_resep") val namaResep: String,
    @SerializedName("chef_name") val chefName: String? = "Chef GROW",
    @SerializedName("rating") val rating: Float? = 0f,
    @SerializedName("foto_resep") val imageUrl: String? = null,
    @SerializedName("usia_rekomendasi") val usiaRekomendasi: String? = "N/A",
    @SerializedName("total_harga") val totalHarga: Double = 0.0,    @SerializedName("waktu_pembuatan") val waktuPembuatan: Int? = 0,
    @SerializedName("nama_kategori") val namaKategori: String? = "N/A",
    @SerializedName("nutrisi") val nutrisi: List<NutrisiItem>? = emptyList(),
    @SerializedName("bahan") val bahan: List<BahanItem>? = emptyList(),
    @SerializedName("langkah_pembuatan") val langkahPembuatan: List<LangkahItem>? = emptyList(),
    @SerializedName("deskripsi") val deskripsi: String? = null,
    val isBookmarked: Boolean = false // Tetap ada untuk bookmark
)

// Model untuk NutrisiItem
data class NutrisiItem(
    @SerializedName("id_resep") val idResep: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("nilai") val nilai: String,
    @SerializedName("satuan") val satuan: String,
    @DrawableRes val iconResource: Int
)

// Model untuk BahanItem
data class BahanItem(
    @SerializedName("id_resep") val idResep: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("jumlah") val jumlah: String,
    @DrawableRes val iconResource: Int
)

// Model untuk LangkahItem
data class LangkahItem(
    @SerializedName("id_resep") val idResep: String,
    @SerializedName("nomor_langkah") val urutan: Int,
    @SerializedName("deskripsi") val deskripsi: String
)

data class ResepResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Resep>? = emptyList()
)

data class ResepSingleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Any? = null
)

// Model untuk request ke API (tetap sama)
data class ResepRequest(
    @SerializedName("nama_resep") val namaResep: String,
    @SerializedName("deskripsi") val deskripsi: String,
    @SerializedName("foto_resep") val fotoResep: String? = null,
    @SerializedName("usia_rekomendasi") val usiaRekomendasi: String,
    @SerializedName("jumlah") val jumlah: Float,
    @SerializedName("rating") val rating: Float? = null,
    @SerializedName("waktu_pembuatan") val waktuPembuatan: Int? = null,
    @SerializedName("nama_kategori") val namaKategori: String,
    @SerializedName("id_nutrisi") val idNutrisi: String,
    @SerializedName("langkah_pembuatan") val langkahPembuatan: List<LangkahRequest>,
    @SerializedName("bahan") val bahan: List<BahanRequest>
)

data class LangkahRequest(
    @SerializedName("nomor_langkah") val nomorLangkah: Int,
    @SerializedName("deskripsi") val deskripsi: String
)

data class BahanRequest(
    @SerializedName("id_bahan") val idBahan: String,
    @SerializedName("jumlah_bahan") val jumlahBahan: Float
)

data class TotalHargaResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: TotalHargaData? = null
)

data class TotalHargaData(
    @SerializedName("total_harga") val totalHarga: Double? = 0.0
)
