package com.example.grow.data.remote

import com.example.grow.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ResepApiService {
    @GET("resep")
    suspend fun getResepList(): ResepResponse

    @GET("resep/{id}")
    suspend fun getResepDetail(@Path("id") id: String): Resep
}