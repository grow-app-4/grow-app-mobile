package com.example.grow.data.api

import com.example.grow.data.model.PertumbuhanResponse
import retrofit2.Response
import retrofit2.http.*

interface PertumbuhanApiService {
    @GET("pertumbuhan")
    suspend fun getAllPertumbuhan(): Response<PertumbuhanResponse>

    @GET("pertumbuhan/{id}")
    suspend fun getPertumbuhanById(@Path("id") id: Int): Response<PertumbuhanResponse>

    @POST("pertumbuhan")
    suspend fun createPertumbuhan(@Body pertumbuhan: HashMap<String, Any>): Response<PertumbuhanResponse>

    @PUT("pertumbuhan/{id}")
    suspend fun updatePertumbuhan(@Path("id") id: Int, @Body pertumbuhan: HashMap<String, Any>): Response<PertumbuhanResponse>

    @DELETE("pertumbuhan/{id}")
    suspend fun deletePertumbuhan(@Path("id") id: Int): Response<PertumbuhanResponse>
}
