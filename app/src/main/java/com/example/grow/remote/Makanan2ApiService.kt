package com.example.grow.remote

import com.example.grow.model.Makanan2
import com.example.grow.model.Makanan2Response
import retrofit2.http.GET

interface Makanan2ApiService {
    @GET("makanan/ibu-hamil")
    suspend fun getMakananIbuHamil(): Makanan2Response
}
