package com.example.grow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.model.Anak
import com.example.grow.model.AnakResponse
import com.example.grow.remote.AnakApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class AnakViewModel @Inject constructor(private val anakApiService: AnakApiService) : ViewModel() {

    private val _anakStateFlow = MutableStateFlow<List<Anak>>(emptyList())
    val anakStateFlow: StateFlow<List<Anak>> get() = _anakStateFlow

    // Fungsi untuk mendapatkan data anak
    fun getAnak() {
        viewModelScope.launch {
            try {
                val response: Response<AnakResponse> = anakApiService.getAllAnak() // Memanggil suspend function
                if (response.isSuccessful) {
                    val anakList = response.body()?.data

                    // Pastikan data tidak null, jika null kirimkan list kosong
                    _anakStateFlow.value = anakList ?: emptyList()
                } else {
                    // Handle error response jika perlu
                }
            } catch (e: Exception) {
                // Handle error jika terjadi kegagalan pada pemanggilan API
            }
        }
    }
}
