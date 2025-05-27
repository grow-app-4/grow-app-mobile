package com.example.grow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrisi")
data class NutrisiEntity(
    @PrimaryKey(autoGenerate = true) val idNutrisi: Int,
    val namaNutrisi: String,
    val satuan: String
)