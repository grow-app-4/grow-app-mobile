package com.example.grow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.UserEntity
import com.example.grow.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class ProfileUpdateViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    data class UiState(
        val originalUser: UserEntity? = null,
        val name: String = "",
        val email: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isUpdateSuccess: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadUserData(token: String, userId: Int) {
        viewModelScope.launch {
            if (userId == 0) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "ID pengguna tidak valid"
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Fetch from API dan update local db dengan token
                userRepository.fetchUserFromApi(token, userId)
                // Observe local db
                userRepository.getUserById(userId).collect { userEntity ->
                    if (userEntity != null) {
                        _uiState.value = _uiState.value.copy(
                            originalUser = userEntity,
                            name = userEntity.name,
                            email = userEntity.email,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Data pengguna tidak ditemukan"
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat data: Koneksi bermasalah"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat data pengguna: ${e.message}"
                )
            }
        }
    }

    fun updateUser(token: String, userId: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            val originalUser = state.originalUser

            if (originalUser == null) {
                _uiState.value = state.copy(
                    errorMessage = "Data pengguna belum dimuat"
                )
                return@launch
            }

            if (userId == 0) {
                _uiState.value = state.copy(
                    errorMessage = "ID pengguna tidak valid"
                )
                return@launch
            }

            val hasChanges = state.name != originalUser.name || state.email != originalUser.email
            if (!hasChanges) {
                _uiState.value = state.copy(
                    errorMessage = "Tidak ada perubahan untuk disimpan"
                )
                return@launch
            }

            if (!validateInputs(state)) {
                _uiState.value = state.copy(
                    errorMessage = "Harap masukkan data yang valid"
                )
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val userEntity = UserEntity(
                id = userId,
                name = state.name,
                email = state.email
            )
            try {
                userRepository.updateUser(token, userEntity)  // <-- pakai token
                _uiState.value = state.copy(
                    isLoading = false,
                    isUpdateSuccess = true,
                    errorMessage = null
                )
            } catch (e: IOException) {
                // Save locally on network failure
                try {
                    userRepository.saveLocalUser(userEntity)
                    _uiState.value = state.copy(
                        isLoading = false,
                        isUpdateSuccess = true,
                        errorMessage = "Data disimpan lokal, akan disinkronkan saat online"
                    )
                } catch (e: Exception) {
                    _uiState.value = state.copy(
                        isLoading = false,
                        errorMessage = "Gagal memperbarui profil: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Gagal memperbarui profil: ${e.message}"
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name.trim())
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email.trim())
    }

    private fun validateInputs(state: UiState): Boolean {
        // Validate name if changed
        if (state.name.isNotBlank() && state.name != state.originalUser?.name) {
            if (state.name.length < 2) return false // Minimum 2 characters
            if (!state.name.matches(Regex("^[a-zA-Z ]+$"))) return false // Letters and spaces only
        }

        // Validate email if changed
        if (state.email.isNotBlank() && state.email != state.originalUser?.email) {
            val emailPattern = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            )
            if (!emailPattern.matcher(state.email).matches()) return false
        }

        return true
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetUpdateSuccess() {
        _uiState.value = _uiState.value.copy(isUpdateSuccess = false)
    }
}