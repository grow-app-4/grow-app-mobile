package com.example.grow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_table")
data class AnakEntity(
    @PrimaryKey val malId: Int,
    val jumlahJaringan: Int,
    val kodeKabupatenKota: Int,
    val kodeProvinsi: Int,
    val namaKabupatenKota: String,
    val namaProvinsi: String,
    val satuan: String,
    val tahun: Int
)