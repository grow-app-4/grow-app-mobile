package com.example.grow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.model.Makanan
import com.example.grow.model.MakananResponse
import com.example.grow.remote.MakananApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MakananViewModel @Inject constructor(private val makananApiService: MakananApiService) : ViewModel() {

    private val _makananStateFlow = MutableStateFlow<List<Makanan>>(emptyList())
    val makananStateFlow: StateFlow<List<Makanan>> get() = _makananStateFlow

    // Fungsi untuk mendapatkan data makanan
    fun getMakanan() {
        viewModelScope.launch {
            try {
                val response: Response<MakananResponse> = makananApiService.getMakanan() // Fungsi suspend dipanggil di sini
                if (response.isSuccessful) {
                    val makananList = response.body()?.data
                    _makananStateFlow.value = makananList ?: emptyList()  // Update LiveData dengan data makanan
                } else {
                    // Handle error response jika perlu
                }
            } catch (e: Exception) {
                // Handle error jika terjadi kegagalan pada pemanggilan API
            }
        }
    }
}
