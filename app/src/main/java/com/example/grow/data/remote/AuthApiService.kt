package com.example.grow.data.remote

import com.example.grow.data.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String): MessageResponse

    @GET("user")
    suspend fun getUser(@Header("Authorization") token: String): User

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): MessageResponse
}