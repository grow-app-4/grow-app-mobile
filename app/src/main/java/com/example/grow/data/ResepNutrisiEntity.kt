package com.example.grow.data

import androidx.room.Entity

@Entity(tableName = "kandungan_nutrisi", primaryKeys = ["idResep", "idNutrisi"])
data class ResepNutrisiEntity(
    val idResep: Int,
    val idNutrisi: Int,
    val jumlah: Float?,
    val satuan: String?
)