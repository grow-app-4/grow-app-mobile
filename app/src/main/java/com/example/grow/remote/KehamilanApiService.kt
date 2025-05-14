package com.example.grow.remote

import com.example.grow.model.KehamilanRequest
import com.example.grow.model.KehamilanResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface KehamilanApiService {
    @POST("kehamilan")
    suspend fun tambahKehamilan(
        @Body request: KehamilanRequest
    ): KehamilanResponse
}
