package com.example.grow.data.model

import com.google.gson.annotations.SerializedName

data class Resep(
    @SerializedName("idMakanan") val idMakanan: String,
    @SerializedName("namaMakanan") val namaMakanan: String,
    @SerializedName("chefName") val chefName: String,
    @SerializedName("rating") val rating: Float? = 0f,
    @SerializedName("imageUrl") val imageUrl: Int? = null // Untuk demo kita gunakan resource drawable ID
)