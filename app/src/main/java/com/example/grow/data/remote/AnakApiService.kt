package com.example.grow.data.remote

import com.example.grow.data.model.AnakResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.Response

interface AnakApiService {
    @GET("anak")
    suspend fun getAllAnak(): Response<AnakResponse>

    @GET("anak/{id}")
    suspend fun getAnakById(@Path("id") id: Int): Response<AnakResponse>

    @POST("anak")
    suspend fun createAnak(@Body anak: HashMap<String, Any>): Response<AnakResponse>

    @PUT("anak/{id}")
    suspend fun updateAnak(@Path("id") id: Int, @Body anak: HashMap<String, Any>): Response<AnakResponse>

    @DELETE("anak/{id}")
    suspend fun deleteAnak(@Path("id") id: Int): Response<AnakResponse>
}