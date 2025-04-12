package com.example.grow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pertumbuhan")
data class PertumbuhanEntity(
    @PrimaryKey(autoGenerate = true) val idPertumbuhan: Int,
    val idAnak: Int,
    val tanggalPencatatan: String,
    val statusStunting: String
)

data class GrowthData(
    val namaJenis: String,
    val nilai: Float
)

data class LatestPertumbuhan(
    val beratBadan: Float?,
    val tinggiBadan: Float?,
    val lingkarKepala: Float?,
    val tanggalPencatatan: String?
)



