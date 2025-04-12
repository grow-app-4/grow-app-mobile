package com.example.grow.data.api

import com.example.grow.data.model.JenisPertumbuhanListResponse
import com.example.grow.data.model.JenisPertumbuhanResponse
import com.example.grow.data.model.PertumbuhanResponse
import com.example.grow.data.model.Pertumbuhan
import com.example.grow.data.model.PertumbuhanRequest
import com.example.grow.data.model.PertumbuhanSingleResponse
import com.example.grow.data.model.StandarPertumbuhanResponse
import retrofit2.Response
import retrofit2.http.*

interface PertumbuhanApiService {
    // Get all growth data
    @GET("pertumbuhan")
    suspend fun getPertumbuhan(): PertumbuhanResponse

    // Create new growth data
    @POST("pertumbuhan")
    suspend fun createPertumbuhan(
        @Body request: PertumbuhanRequest
    ): Response<PertumbuhanSingleResponse>

    // Update existing data
    @PUT("pertumbuhan/{id}")
    suspend fun updatePertumbuhan(
        @Path("id") id: Int,
        @Body pertumbuhan: Pertumbuhan
    ): Response<PertumbuhanSingleResponse>

    // Delete data
    @DELETE("pertumbuhan/{id}")
    suspend fun deletePertumbuhan(
        @Path("id") id: Int
    ): Response<PertumbuhanResponse>

    // Get growth types
    @GET("jenis-pertumbuhan")
    suspend fun getJenisPertumbuhan(): Response<JenisPertumbuhanListResponse>

    @GET("standar-pertumbuhan")
    suspend fun getStandarPertumbuhan(): Response<StandarPertumbuhanResponse>
}