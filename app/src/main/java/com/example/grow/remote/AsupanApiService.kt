package com.example.grow.remote

import com.example.grow.model.AnalisisAsupanRequest
import com.example.grow.model.NutrisiAnalisisResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AsupanApiService {
    @POST("analisis-asupan")
    suspend fun analisisAsupan(
        @Body request: AnalisisAsupanRequest
    ): NutrisiAnalisisResponse
}
