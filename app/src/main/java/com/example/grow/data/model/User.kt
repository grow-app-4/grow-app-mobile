package com.example.grow.data.model

// API response model
data class UserResponse(
    val status: String,
    val data: UserApiModel?
)

data class UserApiModel(
    val id: Int,
    val name: String,
    val email: String,
    val profile_photo: String? = null
)

data class UserUpdateRequest(
    val name: String?,
    val email: String?,
    val profile_photo: String? = null
)