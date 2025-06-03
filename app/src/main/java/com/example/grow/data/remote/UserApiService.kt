package com.example.grow.data.remote

import com.example.grow.data.model.UserResponse
import okhttp3.MultipartBody
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
        @Body body: Map<String, String>
    ): Response<UserResponse>

    @Multipart
    @POST("user/{id}/profile-image")
    suspend fun uploadProfileImage(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>
}

data class ImageUploadResponse(val imageUrl: String) // Response model for image upload