package com.example.grow.model

import com.google.gson.annotations.SerializedName

data class MakananAnak(
    @SerializedName("id_anak") val idAnak: Long,  // id_anak di JSON dipetakan ke idAnak di Kotlin
    @SerializedName("id_makanan") val idMakanan: Long,  // id_makanan di JSON dipetakan ke idMakanan di Kotlin
    @SerializedName("porsi") val porsi: Int  // porsi di JSON dipetakan ke porsi di Kotlin
)
