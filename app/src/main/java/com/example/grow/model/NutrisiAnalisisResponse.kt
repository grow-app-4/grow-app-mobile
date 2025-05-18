package com.example.grow.model

import com.google.gson.annotations.SerializedName

data class NutrisiAnalisisResponse(
    @SerializedName("usia_kehamilan_minggu") val usiaKehamilanMinggu: Float,
    val rentang: String,
    @SerializedName("total_nutrisi_keseluruhan") val totalNutrisi: Map<String, Float>
)


