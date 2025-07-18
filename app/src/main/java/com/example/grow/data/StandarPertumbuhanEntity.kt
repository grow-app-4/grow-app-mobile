package com.example.grow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "standar_pertumbuhan")
data class StandarPertumbuhanEntity(
    @PrimaryKey(autoGenerate = true) val id_standar: Int = 0,
    val id_jenis: Int,
    val usia: Int,
    val jenis_kelamin: String,
    val z_score: Float,
    val nilai: Float
)