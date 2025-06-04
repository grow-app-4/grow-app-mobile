package com.example.grow.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.AnakEntity
import com.example.grow.data.repository.AnakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class UpdateDataAnakViewModel @Inject constructor(
    private val anakRepository: AnakRepository
) : ViewModel() {

    data class UiState(
        val originalChild: AnakEntity? = null,
        val name: String = "",
        val birthDate: String = "",
        val gender: String? = null,
        val profileImageUri: Uri? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isUpdateSuccess: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadChildData(anakId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                anakRepository.getAnakById(anakId).collect { anak ->
                    if (anak != null) {
                        _uiState.value = _uiState.value.copy(
                            originalChild = anak,
                            name = anak.namaAnak,
                            birthDate = anak.tanggalLahir,
                            gender = anak.jenisKelamin,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Data anak tidak ditemukan"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat data anak: ${e.message}"
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name.trim())
    }

    fun updateBirthDate(birthDate: String) {
        _uiState.value = _uiState.value.copy(birthDate = formatDateInput(birthDate))
    }

    fun updateGender(gender: String?) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateProfileImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(profileImageUri = uri)
    }

    fun updateChild(userId: Int, anakId: Int, context: Context) {
        viewModelScope.launch {
            val state = _uiState.value
            val originalChild = state.originalChild

            Log.d("UpdateChild", "State: name=${state.name}, birthDate=${state.birthDate}, gender=${state.gender}, profileImageUri=${state.profileImageUri}")

            if (originalChild == null) {
                _uiState.value = state.copy(errorMessage = "Data anak belum dimuat")
                return@launch
            }

            // Periksa apakah ada perubahan
            val hasChanges = state.name != originalChild.namaAnak ||
                    state.birthDate != originalChild.tanggalLahir ||
                    state.gender != originalChild.jenisKelamin ||
                    state.profileImageUri?.toString() != originalChild.profileImageUri

            if (!hasChanges) {
                _uiState.value = state.copy(errorMessage = "Tidak ada perubahan untuk disimpan")
                return@launch
            }

            // Validasi
            if (!validateInputs(state)) {
                _uiState.value = state.copy(errorMessage = "Harap masukkan semua data yang diperlukan")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                val anak = AnakEntity(
                    idAnak = anakId,
                    idUser = userId,
                    namaAnak = state.name,
                    jenisKelamin = state.gender ?: originalChild.jenisKelamin,
                    tanggalLahir = formatTanggalLahir(state.birthDate),
                    profileImageUri = state.profileImageUri?.toString()
                )

                Log.d("UpdateChild", "Mengirim data ke repository: $anak")
                anakRepository.updateAnak2(anak, state.profileImageUri, context)
                _uiState.value = state.copy(
                    isLoading = false,
                    isUpdateSuccess = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e("UpdateChild", "Error: ${e.message}", e)
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Gagal memperbarui data anak: ${e.message}"
                )
            }
        }
    }

    private fun validateInputs(state: UiState): Boolean {
        // Validasi nama
        if (state.name.isBlank()) {
            Log.d("UpdateChild", "Validasi gagal: Nama anak kosong")
            return false
        }
        if (state.name.length < 2) {
            Log.d("UpdateChild", "Validasi gagal: Nama anak terlalu pendek")
            return false
        }

        // Validasi tanggal lahir
        if (state.birthDate.isBlank() || !isValidDateFormat(state.birthDate)) {
            Log.d("UpdateChild", "Validasi gagal: Tanggal lahir tidak valid")
            return false
        }

        // Validasi jenis kelamin
        if (state.gender == null || state.gender !in listOf("L", "P")) {
            Log.d("UpdateChild", "Validasi gagal: Jenis kelamin tidak valid")
            return false
        }

        return true
    }

    private fun isValidDateFormat(date: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(date, formatter)
            true
        } catch (e: Exception) {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                LocalDate.parse(date, formatter)
                true
            } catch (e: Exception) {
                Log.d("UpdateChild", "Format tanggal tidak valid: $date")
                false
            }
        }
    }

    private fun formatTanggalLahir(birthDate: String): String {
        return try {
            val inputFormatters = listOf(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            )
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            var parsedDate: LocalDate? = null
            for (formatter in inputFormatters) {
                try {
                    parsedDate = LocalDate.parse(birthDate, formatter)
                    break
                } catch (e: Exception) {
                    continue
                }
            }
            parsedDate?.format(outputFormatter) ?: throw IllegalArgumentException("Format tanggal tidak valid: $birthDate")
        } catch (e: Exception) {
            throw IllegalArgumentException("Format tanggal tidak valid: $birthDate")
        }
    }

    private fun formatDateInput(date: String): String {
        return try {
            val inputFormats = listOf(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            )
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            var parsedDate: LocalDate? = null
            for (format in inputFormats) {
                try {
                    parsedDate = LocalDate.parse(date, format)
                    break
                } catch (e: Exception) {
                    continue
                }
            }
            parsedDate?.format(outputFormatter) ?: date
        } catch (e: Exception) {
            date
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetUpdateSuccess() {
        _uiState.value = _uiState.value.copy(isUpdateSuccess = false)
    }
}