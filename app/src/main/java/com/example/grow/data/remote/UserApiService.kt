package com.example.grow.data.remote

import com.example.grow.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApiService {
    @GET("user/{id}")
    suspend fun getUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<UserResponse>

    @PUT("user/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<UserResponse>
}
