package com.example.grow.data.repository

import android.content.Context
import android.net.Uri
import com.example.grow.data.UserDao
import com.example.grow.data.UserEntity
import com.example.grow.data.model.UserUpdateRequest
import com.example.grow.data.remote.UserApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

    suspend fun updateUser(token: String, user: UserEntity, profileImageUri: Uri?, context: Context) {
        withContext(Dispatchers.IO) {
            try {
                // Update local database first
                userDao.updateUser(user)

                val bearerToken = "Bearer $token"
                var lastException: Exception? = null
                repeat(3) { attempt ->
                    try {
                        val response = if (profileImageUri != null) {
                            // Handle photo upload
                            val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                            context.contentResolver.openInputStream(profileImageUri)?.use { input ->
                                file.outputStream().use { output -> input.copyTo(output) }
                            }
                            val requestBody = file.asRequestBody("image/jpeg".toMediaType())
                            val profilePhotoPart = MultipartBody.Part.createFormData("profile_photo", file.name, requestBody)
                            userApiService.updateUserWithPhoto(
                                bearerToken,
                                user.id,
                                name = user.name.toRequestBody("text/plain".toMediaType()),
                                email = user.email.toRequestBody("text/plain".toMediaType()),
                                profilePhoto = profilePhotoPart
                            )
                        } else {
                            // Handle update without photo
                            userApiService.updateUser(
                                bearerToken,
                                user.id,
                                UserUpdateRequest(
                                    name = user.name,
                                    email = user.email,
                                    profile_photo = user.profileImageUri
                                )
                            )
                        }

                        if (response.isSuccessful && response.body()?.data != null) {
                            response.body()?.data?.let {
                                userDao.updateUser(
                                    UserEntity(
                                        id = it.id,
                                        name = it.name,
                                        email = it.email,
                                        profileImageUri = it.profile_photo
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
                // Fallback to local update
                userDao.updateUser(user.copy(profileImageUri = profileImageUri?.toString()))
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
                                profileImageUri = it.profile_photo
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
}