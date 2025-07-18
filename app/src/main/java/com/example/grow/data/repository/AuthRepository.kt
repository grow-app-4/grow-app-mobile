package com.example.grow.data.repository

import com.example.grow.data.model.*
import com.example.grow.data.remote.AuthApiService
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApiService
) {
    suspend fun register(request: RegisterRequest) = api.register(request)
    suspend fun login(request: LoginRequest) = api.login(request)
    suspend fun logout(token: String) = api.logout("Bearer $token")
    suspend fun getUser(token: String) = api.getUser("Bearer $token")
    suspend fun forgotPassword(request: ForgotPasswordRequest) = api.forgotPassword(request)
    suspend fun verifyResetCode(request: VerifyResetCodeRequest) = api.verifyResetCode(request)
    suspend fun resetPassword(request: ResetPasswordRequest) = api.resetPassword(request)
    suspend fun verifyEmailCode(request: VerifyResetCodeRequest) = api.verifyEmailCode(request)
    suspend fun resendVerificationCode(request: ForgotPasswordRequest) = api.resendVerificationCode(request)
}
