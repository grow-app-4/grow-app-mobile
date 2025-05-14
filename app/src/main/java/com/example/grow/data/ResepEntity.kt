package com.example.grow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "makanan")
data class ResepEntity(
    @PrimaryKey(autoGenerate = false) val idMakanan: Int,
    val namaMakanan: String,
    val kategori: String,
    val ukuranPorsi: Int,
    val usiaRekomendasi: Int?,
    val rating: Float? = 0.0f,
    val waktuPersiapan: Int? = 0
)

@Entity(tableName = "bahan_baku")
data class BahanEntity(
    @PrimaryKey(autoGenerate = false) val idBahan: Int,
    val namaBahan: String,
    val namaBahanEn: String?,
    val satuan: String,
    val hargaBahan: Float
)

@Entity(tableName = "makanan_bahan", primaryKeys = ["idMakanan", "idBahan"])
data class ResepBahanEntity(
    val idMakanan: Int,
    val idBahan: Int,
    val jumlah: Float?,
    val satuan: String?
)

@Entity(tableName = "langkah_pembuatan", primaryKeys = ["idMakanan", "urutan"])
data class LangkahPembuatanEntity(
    val idMakanan: Int,
    val urutan: Int,
    val deskripsi: String,
    val waktu: Int
)

@Entity(tableName = "nutrisi")
data class NutrisiEntity(
    @PrimaryKey(autoGenerate = false) val idNutrisi: Int,
    val namaNutrisi: String,
    val satuan: String
)

@Entity(tableName = "makanan_nutrisi", primaryKeys = ["idMakanan", "idNutrisi"])
data class ResepNutrisiEntity(
    val idMakanan: Int,
    val idNutrisi: Int,
    val jumlah: Float?,
    val satuan: String?
)