package com.example.grow.remote

import com.example.grow.model.KehamilanRequest
import com.example.grow.model.KehamilanResponse
import com.example.grow.model.UsiaKehamilanResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface KehamilanApiService {
    @POST("kehamilan")
    suspend fun tambahKehamilan(
        @Body request: KehamilanRequest
    ): KehamilanResponse

    @GET("kehamilan/usia/{userId}")
    suspend fun getUsiaKehamilan(@Path("userId") userId: Int): UsiaKehamilanResponse
}
