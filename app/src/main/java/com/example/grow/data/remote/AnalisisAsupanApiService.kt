package com.example.grow.data.remote

import com.example.grow.data.model.AnalisisAsupanRequest
import com.example.grow.data.model.AnalisisAsupanResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AnalisisAsupanApiService {
    @POST("analisis-asupan")
    suspend fun analisisAsupan(
        @Body request: AnalisisAsupanRequest
    ): AnalisisAsupanResponse
}