package com.example.grow.data.remote

import com.example.grow.data.model.ResepEntity
import com.example.grow.data.model.BahanEntity
import com.example.grow.data.model.LangkahPembuatanEntity
import com.example.grow.data.model.NutrisiEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ResepApiService {
    @GET("makanan")
    suspend fun getAllResep(): Response<List<ResepEntity>>

    @GET("bahan_baku")
    suspend fun getAllBahan(): Response<List<BahanEntity>>

    @GET("makanan/{id}/bahan")
    suspend fun getBahanByResep(@Path("id") idMakanan: Int): Response<List<BahanEntity>>

    @GET("makanan/{id}/langkah")
    suspend fun getLangkahByResep(@Path("id") idMakanan: Int): Response<List<LangkahPembuatanEntity>>

    @GET("makanan/{id}/nutrisi")
    suspend fun getNutrisiByResep(@Path("id") idMakanan: Int): Response<List<NutrisiEntity>>
}