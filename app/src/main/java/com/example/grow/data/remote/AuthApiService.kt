package com.example.grow.data.remote

import com.example.grow.data.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String): Response<MessageResponse>

    @GET("user")
    suspend fun getUser(@Header("Authorization") token: String): Response<User>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("verify-reset-code")
    suspend fun verifyResetCode(@Body request: VerifyResetCodeRequest): Response<VerifyResetCodeResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>

    @POST("verify-email")
    suspend fun verifyEmailCode(@Body request: VerifyResetCodeRequest): Response<AuthResponse>

    @POST("resend-verification-code")
    suspend fun resendVerificationCode(@Body request: ForgotPasswordRequest): Response<MessageResponse>
}

