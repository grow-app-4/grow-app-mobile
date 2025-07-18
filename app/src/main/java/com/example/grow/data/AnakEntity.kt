package com.example.grow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anak")
data class AnakEntity(
    @PrimaryKey(autoGenerate = true) val idAnak: Int,
    val idUser: Int,
    val namaAnak: String,
    val jenisKelamin: String,
    val tanggalLahir: String,
    val profileImageUri: String?
)