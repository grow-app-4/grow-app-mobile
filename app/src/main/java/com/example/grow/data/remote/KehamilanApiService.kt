package com.example.grow.data.remote

import com.example.grow.data.model.KehamilanRequest
import com.example.grow.data.model.KehamilanResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface KehamilanApiService {
    @POST("kehamilan")
    suspend fun tambahKehamilan(
        @Body request: KehamilanRequest
    ): KehamilanResponse
}