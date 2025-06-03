package com.example.grow.data.repository

import android.content.Context
import android.net.Uri
import com.example.grow.data.UserDao
import com.example.grow.data.UserEntity
import com.example.grow.data.remote.UserApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val userApiService: UserApiService
) {

    fun getUserById(userId: Int): Flow<UserEntity?> {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(token: String, user: UserEntity) {
        withContext(Dispatchers.IO) {
            try {
                userDao.updateUser(user)
                var lastException: Exception? = null
                repeat(3) { attempt ->
                    try {
                        val bearerToken = "Bearer $token"
                        val body = mutableMapOf<String, String>(
                            "name" to user.name,
                            "email" to user.email
                        )
                        // Tambahkan profileImageUri ke body hanya jika API mendukungnya
                        user.profileImageUri?.let { body["profileImageUri"] = it }
                        val response = userApiService.updateUser(
                            bearerToken,
                            user.id,
                            body
                        )
                        if (response.isSuccessful && response.body()?.data != null) {
                            response.body()?.data?.let {
                                userDao.updateUser(
                                    UserEntity(
                                        id = it.id,
                                        name = it.name,
                                        email = it.email,
                                        profileImageUri = it.profileImageUri // Sekarang field ini ada
                                    )
                                )
                            }
                            return@repeat
                        } else {
                            lastException = Exception("API update failed: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        lastException = e
                        if (e is IOException && attempt < 2) {
                            delay(1000)
                        } else {
                            return@repeat
                        }
                    }
                }
                lastException?.let { throw it }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun saveLocalUser(user: UserEntity) {
        withContext(Dispatchers.IO) {
            userDao.updateUser(user)
        }
    }

    suspend fun fetchUserFromApi(token: String, userId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val bearerToken = "Bearer $token"
                val response = userApiService.getUser(bearerToken, userId)
                if (response.isSuccessful && response.body()?.data != null) {
                    response.body()?.data?.let {
                        userDao.insertUser(
                            UserEntity(
                                id = it.id,
                                name = it.name,
                                email = it.email,
                                profileImageUri = it.profileImageUri // Sekarang field ini ada
                            )
                        )
                    }
                } else {
                    throw Exception("Failed to fetch user: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun uploadProfileImage(token: String, userId: Int, imageUri: Uri, context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val requestBody = file.asRequestBody("image/jpeg".toMediaType())
                val part = MultipartBody.Part.createFormData("image", file.name, requestBody)
                val response = userApiService.uploadProfileImage("Bearer $token", userId, part)
                if (response.isSuccessful) {
                    response.body()?.imageUrl // Return nilai ini
                } else {
                    throw Exception("Image upload failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}