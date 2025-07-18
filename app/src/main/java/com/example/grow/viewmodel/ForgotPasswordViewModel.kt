package com.example.grow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.ForgotPasswordRequest
import com.example.grow.data.model.ForgotPasswordUiState
import com.example.grow.data.model.ResetPasswordRequest
import com.example.grow.data.model.VerifyResetCodeRequest
import com.example.grow.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun sendForgotPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState(isLoading = true, errorMessage = null)
            try {
                val response = authRepository.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful && response.body()?.message != null) {
                    _uiState.value = ForgotPasswordUiState(
                        isLoading = false,
                        successMessage = response.body()?.message
                    )
                } else {
                    _uiState.value = ForgotPasswordUiState(
                        isLoading = false,
                        errorMessage = response.errorBody()?.string() ?: "Gagal mengirim kode reset"
                    )
                }
            } catch (e: IOException) {
                _uiState.value = ForgotPasswordUiState(
                    isLoading = false,
                    errorMessage = "Error jaringan: ${e.message}"
                )
            } catch (e: Exception) {
                _uiState.value = ForgotPasswordUiState(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    fun verifyResetCode(email: String, code: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState(isLoading = true, errorMessage = null)
            try {
                val response = authRepository.verifyResetCode(VerifyResetCodeRequest(email, code))
                if (response.isSuccessful && response.body()?.message != null) {
                    _uiState.value = ForgotPasswordUiState(
                        isLoading = false,
                        successMessage = response.body()?.message,
                        resetToken = response.body()?.reset_token
                    )
                } else {
                    _uiState.value = ForgotPasswordUiState(
                        isLoading = false,
                        errorMessage = response.errorBody()?.string() ?: "Kode reset tidak valid"
                    )
                }
            } catch (e: IOException) {
                _uiState.value = ForgotPasswordUiState(
                    isLoading = false,
                    errorMessage = "Error jaringan: ${e.message}"
                )
            } catch (e: Exception) {
                _uiState.value = ForgotPasswordUiState(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    fun resetPassword(email: String, resetToken: String, password: String, passwordConfirmation: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState(isLoading = true, errorMessage = null)
            try {
                val response = authRepository.resetPassword(
                    ResetPasswordRequest(email, resetToken, password, passwordConfirmation)
                )
                if (response.isSuccessful && response.body()?.message != null) {
                    _uiState.value = ForgotPasswordUiState(
                        isLoading = false,
                        successMessage = response.body()?.message
                    )
                } else {
                    _uiState.value = ForgotPasswordUiState(
                        isLoading = false,
                        errorMessage = response.errorBody()?.string() ?: "Gagal mereset kata sandi"
                    )
                }
            } catch (e: IOException) {
                _uiState.value = ForgotPasswordUiState(
                    isLoading = false,
                    errorMessage = "Error jaringan: ${e.message}"
                )
            } catch (e: Exception) {
                _uiState.value = ForgotPasswordUiState(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = ForgotPasswordUiState(
            isLoading = false,
            errorMessage = null,
            successMessage = null,
            resetToken = _uiState.value.resetToken
        )
    }
}