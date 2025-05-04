package com.example.grow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.model.MakananAnak
import com.example.grow.remote.MakananAnakApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MakananAnakViewModel @Inject constructor(
    private val makananAnakApiService: MakananAnakApiService
) : ViewModel() {

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> get() = _toastMessage

    fun storeMakananAnak(idAnak: Long, idMakanan: Long, porsi: Int) {
        val makananAnak = MakananAnak(idAnak, idMakanan, porsi)

        viewModelScope.launch {
            try {
                val response: Response<MakananAnak> = makananAnakApiService.storeMakananAnak(makananAnak)
                if (response.isSuccessful) {
                    _toastMessage.value = "Data berhasil disimpan"
                } else {
                    _toastMessage.value = "Gagal menyimpan data"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Terjadi kesalahan: ${e.message}"
            }
        }
    }
    fun showToastMessage(message: String) {
        _toastMessage.value = message
    }
}
