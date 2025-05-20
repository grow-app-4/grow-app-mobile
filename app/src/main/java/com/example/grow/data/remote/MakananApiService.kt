package com.example.grow.data.remote

import com.example.grow.data.model.MakananResponse
import retrofit2.http.GET

interface MakananApiService {
    @GET("makanan/ibu-hamil")
    suspend fun getMakananIbuHamil(): MakananResponse
}