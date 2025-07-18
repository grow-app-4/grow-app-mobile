package com.example.grow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resep")
data class ResepEntity(
    @PrimaryKey(autoGenerate = false) val idResep: Int,
    val namaResep: String,
    val deskripsi: String,
    val fotoResep: String?,
    val usiaRekomendasi: String,
    val jumlah: Float,
    val rating: Float?,
    val waktuPembuatan: Int?,
    val namaKategori: String,
    val idNutrisi: Int
)

@Entity(tableName = "langkah_pembuatan", primaryKeys = ["resepId", "nomorLangkah"])
data class LangkahPembuatanEntity(
    val resepId: Int,
    val nomorLangkah: Int,
    val deskripsi: String,
    val timestamps: String? // Jika perlu
)

@Entity(tableName = "bahan_baku")
data class BahanEntity(
    @PrimaryKey(autoGenerate = false) val idBahan: Int,
    val namaBahan: String,
    val satuan: String,
    val hargaBahan: Float
)

@Entity(tableName = "resep_bahan", primaryKeys = ["idResep", "idBahan"])
data class ResepBahanEntity(
    val idResep: Int,
    val idBahan: Int,
    val jumlahBahan: Float
)
