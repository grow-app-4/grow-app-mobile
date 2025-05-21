package com.example.grow.data.remote

import com.example.grow.data.model.Anak
import com.example.grow.data.model.AnakRequest
import com.example.grow.data.model.AnakResponse
import com.example.grow.data.model.AnakSingleResponse
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
    suspend fun getAnakById(@Path("id") id: Int): Response<Anak>

    @POST("anak")
    suspend fun createAnak(@Body anak: HashMap<String, Any>): Response<AnakResponse>

    @POST("anak")
    suspend fun createAnak(@Body anakRequest: AnakRequest): Response<AnakSingleResponse>

    @PUT("anak/{id}")
    suspend fun updateAnak(@Path("id") id: Int, @Body anak: HashMap<String, Any>): Response<AnakSingleResponse>

    @DELETE("anak/{id}")
    suspend fun deleteAnak(@Path("id") id: Int): Response<AnakResponse>
}
