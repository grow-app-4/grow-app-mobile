package com.example.grow.data.repository

import androidx.datastore.core.IOException
import com.example.grow.data.UserDao
import com.example.grow.data.UserEntity
import com.example.grow.data.remote.UserApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
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
                        val response = userApiService.updateUser(
                            bearerToken,
                            user.id,
                            mapOf(
                                "name" to user.name,
                                "email" to user.email
                            )
                        )
                        if (response.isSuccessful && response.body()?.data != null) {
                            response.body()?.data?.let {
                                userDao.updateUser(
                                    UserEntity(
                                        id = it.id,
                                        name = it.name,
                                        email = it.email
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
                                email = it.email
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