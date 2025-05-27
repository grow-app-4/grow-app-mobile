package com.example.grow.viewmodel

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

    fun updateChild(userId: Int, anakId: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            val originalChild = state.originalChild

            Log.d("UpdateChild", "Memulai proses update anak (userId=$userId, anakId=$anakId)")

            if (originalChild == null) {
                Log.d("UpdateChild", "Gagal: Data anak belum dimuat")
                _uiState.value = state.copy(
                    errorMessage = "Data anak belum dimuat"
                )
                return@launch
            }

            // Periksa apakah ada perubahan
            val hasChanges = state.name != originalChild.namaAnak ||
                    state.birthDate != originalChild.tanggalLahir ||
                    state.gender != originalChild.jenisKelamin

            Log.d("UpdateChild", "Perubahan terdeteksi: $hasChanges")

            if (!hasChanges) {
                Log.d("UpdateChild", "Tidak ada perubahan data")
                _uiState.value = state.copy(
                    errorMessage = "Tidak ada perubahan untuk disimpan"
                )
                return@launch
            }

            // Validasi
            if (!validateInputs(state)) {
                Log.d("UpdateChild", "Validasi gagal")
                _uiState.value = state.copy(
                    errorMessage = "Harap masukkan data yang valid"
                )
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            Log.d("UpdateChild", "Memulai proses penyimpanan ke database")

            try {
                val anak = AnakEntity(
                    idAnak = anakId,
                    idUser = userId,
                    namaAnak = state.name,
                    jenisKelamin = state.gender ?: originalChild.jenisKelamin,
                    tanggalLahir = state.birthDate.ifBlank { originalChild.tanggalLahir }
                )

                Log.d("UpdateChild", "Data yang akan disimpan: $anak")

                Log.d("UpdateChild", "Mengirim data ke server...")
                anakRepository.updateAnak(anak)
                Log.d("UpdateChild", "Berhasil update ke server")


                Log.d("UpdateChild", "Update berhasil")
                _uiState.value = state.copy(
                    isLoading = false,
                    isUpdateSuccess = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e("UpdateChild", "Terjadi kesalahan saat update: ${e.message}", e)
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Gagal memperbarui data anak: ${e.message}"
                )
            }
        }
    }

    private fun validateInputs(state: UiState): Boolean {
        // Validasi nama jika diubah
        if (state.name.isNotBlank() && state.name != state.originalChild?.namaAnak) {
            if (state.name.length < 2) return false // Contoh: minimal 2 karakter
        }

        // Validasi tanggal lahir jika diubah
        if (state.birthDate.isNotBlank() && state.birthDate != state.originalChild?.tanggalLahir) {
            if (!isValidDateFormat(state.birthDate)) return false
        }

        // Validasi jenis kelamin jika diubah
        if (state.gender != null && state.gender != state.originalChild?.jenisKelamin) {
            if (state.gender !in listOf("L", "P")) return false
        }

        return true
    }

    private fun isValidDateFormat(date: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(date, formatter)
            true
        } catch (e: Exception) {
            false
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