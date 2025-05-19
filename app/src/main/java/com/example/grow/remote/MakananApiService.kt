package com.example.grow.remote

import com.example.grow.model.MakananResponse
import retrofit2.http.GET

interface MakananApiService {
    @GET("makanan/ibu-hamil")
    suspend fun getMakananIbuHamil(): MakananResponse
}
