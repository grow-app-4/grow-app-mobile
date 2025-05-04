package com.example.grow.remote

import retrofit2.Response
import retrofit2.http.GET
import com.example.grow.model.MakananResponse

interface MakananApiService {
    @GET("makanan")
    suspend fun getMakanan(): Response<MakananResponse>
}
