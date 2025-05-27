package com.example.grow.data.remote

import com.example.grow.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ResepApiService {
    // Get all recipes
    @GET("resep")
    suspend fun getAllResep(): Response<ResepResponse>

    // Get detailed recipe information (all in one endpoint)
    @GET("resep/{id}/detail")
    suspend fun getDetailResep(
        @Path("id") id: String
    ): Response<ResepSingleResponse>

    // Create new recipe
    @POST("resep")
    suspend fun createResep(
        @Body request: ResepRequest
    ): Response<ResepSingleResponse>

    // Update existing recipe
    @PUT("resep/{id}")
    suspend fun updateResep(
        @Path("id") id: String,
        @Body resep: Resep
    ): Response<ResepSingleResponse>

    // Delete recipe
    @DELETE("resep/{id}")
    suspend fun deleteResep(
        @Path("id") id: String
    ): Response<ResepResponse>

    // Legacy endpoints (will be removed after migration to single endpoint)
    // Get ingredients by recipe
    @GET("resep/{id}/bahan")
    suspend fun getBahanByResep(
        @Path("id") id: String
    ): Response<ResepResponse>

    // Get preparation steps by recipe
    @GET("resep/{id}/langkah-pembuatan")
    suspend fun getLangkahByResep(
        @Path("id") id: String
    ): Response<ResepResponse>

    // Get nutrition by recipe
    @GET("resep/{id}/nutrisi")
    suspend fun getNutrisiByResep(
        @Path("id") id: String
    ): Response<ResepResponse>

    @GET("resep/{id}/total-harga")
    suspend fun getTotalHarga(
        @Path("id") id: String
    ): Response<TotalHargaResponse>
}