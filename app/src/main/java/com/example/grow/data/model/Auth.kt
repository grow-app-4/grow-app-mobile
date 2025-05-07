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
