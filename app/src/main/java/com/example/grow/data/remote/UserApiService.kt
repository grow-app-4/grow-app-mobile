package com.example.grow.data.remote

import com.example.grow.data.model.UserResponse
import com.example.grow.data.model.UserUpdateRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

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
        @Body request: UserUpdateRequest
    ): Response<UserResponse>

    @Multipart
    @PUT("user/{id}")
    suspend fun updateUserWithPhoto(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part("name") name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part profilePhoto: MultipartBody.Part?
    ): Response<UserResponse>
}