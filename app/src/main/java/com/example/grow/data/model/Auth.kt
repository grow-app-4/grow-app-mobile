package com.example.grow.data.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class AuthResponse(
    val message: String,
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val name: String,
    val email: String
)

data class MessageResponse(
    val message: String
)


data class SimpleResponse(
    val message: String
)

data class VerifyResetCodeRequest(
    val email: String,
    val code: String
)

data class ResetPasswordRequest(
    val email: String,
    val reset_token: String,
    val password: String,
    val password_confirmation: String
)


data class VerifyResetCodeResponse(
    val message: String,
    val reset_token: String? = null
)

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)