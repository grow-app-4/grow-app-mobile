package com.example.grow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jenis_pertumbuhan")
data class JenisPertumbuhanEntity(
    @PrimaryKey val idJenis: Int,
    val namaJenis: String
)
