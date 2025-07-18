package com.example.grow.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.Resource
import com.example.grow.data.UserEntity
import com.example.grow.data.model.AuthResponse
import com.example.grow.data.model.AuthUiState
import com.example.grow.data.model.ForgotPasswordRequest
import com.example.grow.data.model.LoginRequest
import com.example.grow.data.model.RegisterRequest
import com.example.grow.data.model.VerifyResetCodeRequest
import com.example.grow.data.repository.AuthRepository
import com.example.grow.data.repository.UserRepository
import com.example.grow.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponse>>(Resource.Loading())
    val loginState: StateFlow<Resource<AuthResponse>> = _loginState

    private val _userIdState = MutableStateFlow<Int?>(null)
    val userIdState: StateFlow<Int?> = _userIdState

    private val _forgotPasswordState = MutableStateFlow<Resource<String>>(Resource.Loading())
    val forgotPasswordState: StateFlow<Resource<String>> = _forgotPasswordState

    private val _verifyEmailState = MutableStateFlow<Resource<AuthResponse>>(Resource.Loading())
    val verifyEmailState: StateFlow<Resource<AuthResponse>> = _verifyEmailState

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun getUserById(userId: Int): Flow<UserEntity?> {
        return userRepository.getUserById(userId)
    }

    fun login(email: String, password: String, context: Context) {
        viewModelScope.launch {
            try {
                _loginState.value = Resource.Loading()
                val response = repository.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!! // Ambil AuthResponse dari body
                    _loginState.value = Resource.Success(authResponse)
                    _userIdState.value = authResponse.user.id
                    authResponse.token?.let {
                        SessionManager.saveLoginSession(context, authResponse.user.id,
                            it
                        )
                    }
                } else {
                    _loginState.value = Resource.Error("Login gagal: ${response.errorBody()?.string() ?: "Respon tidak valid"}")
                }
            } catch (e: Exception) {
                _loginState.value = Resource.Error(e.localizedMessage ?: "Error saat login")
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            SessionManager.clearSession(context)
            _loginState.value = Resource.Loading() // Reset login state
            _userIdState.value = null
            Log.d("AuthViewModel", "Logged out, loginState reset")
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            try {
                _forgotPasswordState.value = Resource.Loading()
                val response = repository.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful && response.body() != null) {
                    val messageResponse = response.body()!! // Ambil MessageResponse
                    _forgotPasswordState.value = Resource.Success(messageResponse.message)
                    Log.d("AuthViewModel", "Forgot password success: ${messageResponse.message}")
                } else {
                    _forgotPasswordState.value = Resource.Error(
                        response.errorBody()?.string() ?: "Gagal mengirim permintaan reset password"
                    )
                    Log.e("AuthViewModel", "Forgot password error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = Resource.Error(
                    e.localizedMessage ?: "Gagal mengirim permintaan reset password"
                )
                Log.e("AuthViewModel", "Forgot password error: ${e.message}")
            }
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return SessionManager.isLoggedIn(context)
    }

    fun getUserId(context: Context): Int {
        return SessionManager.getUserId(context)
    }

    fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        context: Context
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true, errorMessage = null)
            try {
                val response = repository.register(
                    RegisterRequest(name, email, password, passwordConfirmation)
                )
                if (response.isSuccessful && response.body()?.message == "success") {
                    response.body()?.let { authResponse ->
                        authResponse.token?.let {
                            SessionManager.saveLoginSession(
                                context = context,
                                userId = authResponse.user.id,
                                token = it
                            )
                        }
                        _uiState.value = AuthUiState(
                            isLoading = false,
                            successMessage = authResponse.message
                        )
                    }
                } else {
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Gagal registrasi"
                    } catch (e: Exception) {
                        "Gagal registrasi: ${e.message}"
                    }
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
            } catch (e: IOException) {
                _uiState.value = AuthUiState(
                    isLoading = false,
                    errorMessage = "Error jaringan: ${e.message}"
                )
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    fun verifyEmailCode(email: String, code: String, context: Context) {
        viewModelScope.launch {
            try {
                _verifyEmailState.value = Resource.Loading()
                val response = repository.verifyEmailCode(VerifyResetCodeRequest(email, code))
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    _verifyEmailState.value = Resource.Success(authResponse)
                    _userIdState.value = authResponse.user.id
                    authResponse.token?.let {
                        SessionManager.saveLoginSession(context, authResponse.user.id,
                            it
                        )
                    }
                } else {
                    _verifyEmailState.value = Resource.Error(
                        response.errorBody()?.string() ?: "Gagal memverifikasi kode"
                    )
                }
            } catch (e: Exception) {
                _verifyEmailState.value = Resource.Error(
                    e.localizedMessage ?: "Error saat memverifikasi kode"
                )
            }
        }
    }

    fun resendVerificationCode(email: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState(isLoading = true, errorMessage = null)
                val response = repository.resendVerificationCode(ForgotPasswordRequest(email))
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        successMessage = response.body()!!.message
                    )
                } else {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = response.errorBody()?.string() ?: "Gagal mengirim ulang kode"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error saat mengirim ulang kode"
                )
            }
        }
    }

    fun setErrorMessage(message: String) {
        _uiState.value = AuthUiState(
            isLoading = false,
            errorMessage = message,
            successMessage = null
        )
    }

    fun clearMessages() {
        _uiState.value = AuthUiState(isLoading = false, errorMessage = null, successMessage = null)
    }
}