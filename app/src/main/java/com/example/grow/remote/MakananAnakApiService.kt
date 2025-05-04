package com.example.grow.remote

import com.example.grow.model.MakananAnak
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MakananAnakApiService {

    @POST("/api/makanan-anak")
    suspend fun storeMakananAnak(@Body makananAnak: MakananAnak): Response<MakananAnak>
}
