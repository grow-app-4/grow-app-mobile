package com.example.grow.data.model

import com.example.grow.R
import com.google.gson.annotations.SerializedName

data class ResepResponse(
    val data: List<Resep>
)

data class Resep(
    @SerializedName("id_resep") val idResep: String,
    @SerializedName("nama_resep") val namaResep: String,
    @SerializedName("deskripsi") val deskripsi: String?,
    @SerializedName("foto_resep") val imageUrl: String?,
    @SerializedName("usia_rekomendasi") val usiaRekomendasi: String?,
    @SerializedName("jumlah") val jumlah: String?,
    @SerializedName("rating") val ratingString: String?,
    @SerializedName("waktu_pembuatan") val waktuPembuatan: Int?,
    @SerializedName("nama_kategori") val namaKategori: String?,
    @SerializedName("id_nutrisi") val idNutrisi: String?,
    @SerializedName("total_harga") val totalHarga: Double?,
    @SerializedName("nutrisi") val nutrisi: List<NutrisiItem>?,
    @SerializedName("bahan_baku") val bahan: List<BahanItem>?,
    @SerializedName("langkah_pembuatan") val langkahPembuatan: List<LangkahItem>?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
) {
    val rating: Float?
        get() = ratingString?.toFloatOrNull() // Konversi String ke Float
}

data class BahanItem(
    @SerializedName("id_bahan") val idBahan: String,
    @SerializedName("nama_bahan") val nama: String,
    @SerializedName("satuan") val satuan: String,
    @SerializedName("harga_bahan") val hargaBahan: Double,
    @SerializedName("pivot") val pivot: Pivot
) {
    // Placeholder untuk icon, bisa disesuaikan dengan logika aplikasi
    val iconResource: Int
        get() = when (nama.lowercase()) {
            "beras merah" -> R.drawable.ic_star
            "kentang" -> R.drawable.ic_star
            "wortel" -> R.drawable.ic_star
            else -> R.drawable.ic_star
        }
}

data class Pivot(
    @SerializedName("jumlah_bahan") val jumlahBahan: String
) {
    // Format jumlah_bahan untuk tampilan
    val jumlah: String
        get() = "$jumlahBahan $satuan"

    // Asumsi satuan ada di BahanItem, bisa disesuaikan
    private val satuan: String
        get() = "gram" // Ganti dengan logika sesuai kebutuhan
}

data class LangkahItem(
    @SerializedName("id") val id: String,
    @SerializedName("resep_id") val resepId: String,
    @SerializedName("nomor_langkah") val urutan: Int,
    @SerializedName("deskripsi") val deskripsi: String
)

data class NutrisiItem(
    @SerializedName("id_nutrisi") val idNutrisi: String,
    @SerializedName("nama_nutrisi") val nama: String,
    @SerializedName("pivot") val pivot: NutrisiPivot
) {
    // Placeholder untuk icon, bisa disesuaikan
    val iconResource: Int
        get() = when (nama.lowercase()) {
            "protein" -> R.drawable.ic_star
            "karbohidrat" -> R.drawable.ic_star
            else -> R.drawable.ic_star
        }
}

data class NutrisiPivot(
    @SerializedName("jumlah") val nilai: Double,
    @SerializedName("satuan") val satuan: String
)

