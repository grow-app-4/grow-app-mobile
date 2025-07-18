package com.example.grow.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
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
                Log.d("UPDATE_USER", "Local update: name=${user.name}, email=${user.email}, profileImageUri=${user.profileImageUri}")

                val bearerToken = "Bearer $token"
                var lastException: Exception? = null
                repeat(3) { attempt ->
                    try {
                        val response = if (profileImageUri != null && !profileImageUri.toString().startsWith("http")) {
                            // Handle new local file upload
                            Log.d("UPDATE_USER", "Processing local file: $profileImageUri")
                            val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                            context.contentResolver.openInputStream(profileImageUri)?.use { input ->
                                file.outputStream().use { output -> input.copyTo(output) }
                            }
                            val requestBody = file.asRequestBody("image/jpeg".toMediaType())
                            val profilePhotoPart = MultipartBody.Part.createFormData("profile_photo", file.name, requestBody)
                            Log.d("UPDATE_USER", "Sending POST with photo: name=${user.name}, email=${user.email}")
                            userApiService.updateUserWithPhoto(
                                bearerToken,
                                user.id,
                                name = user.name.toRequestBody("text/plain".toMediaType()),
                                email = user.email.toRequestBody("text/plain".toMediaType()),
                                profilePhoto = profilePhotoPart
                            )
                        } else {
                            // Only send changed fields (exclude profile_photo unless explicitly changed)
                            Log.d("UPDATE_USER", "Sending POST without photo: name=${user.name}, email=${user.email}")
                            userApiService.updateUser(
                                bearerToken,
                                user.id,
                                UserUpdateRequest(
                                    name = user.name,
                                    email = user.email,
                                    profile_photo = null // Don’t send existing profile_photo URL
                                )
                            )
                        }

                        Log.d("UPDATE_USER", "Response: code=${response.code()}, success=${response.isSuccessful}")
                        if (!response.isSuccessful) {
                            val errorBody = response.errorBody()?.string()
                            Log.e("UPDATE_USER", "Error body: $errorBody")
                        } else {
                            Log.d("UPDATE_USER", "Response body: ${response.body()?.data}")
                        }

                        if (response.isSuccessful && response.body()?.data != null) {
                            response.body()?.data?.let {
                                // Preserve existing profileImageUri if not updated
                                val updatedProfileUri = if (it.profile_photo != null) it.profile_photo else user.profileImageUri
                                userDao.updateUser(
                                    UserEntity(
                                        id = it.id,
                                        name = it.name,
                                        email = it.email,
                                        profileImageUri = updatedProfileUri
                                    )
                                )
                                Log.d("UPDATE_USER", "API update saved: name=${it.name}, email=${it.email}, profileImageUri=$updatedProfileUri")
                            }
                            return@repeat
                        } else {
                            lastException = Exception("API update failed: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        lastException = e
                        Log.e("UPDATE_USER", "Attempt ${attempt + 1} failed: ${e.message}")
                        if (e is IOException && attempt < 2) {
                            delay(1000)
                        } else {
                            return@repeat
                        }
                    }
                }
                lastException?.let { throw it }
            } catch (e: Exception) {
                Log.e("UPDATE_USER", "Fallback local update: name=${user.name}, email=${user.email}, error=${e.message}")
                userDao.updateUser(user.copy(profileImageUri = profileImageUri?.toString() ?: user.profileImageUri))
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