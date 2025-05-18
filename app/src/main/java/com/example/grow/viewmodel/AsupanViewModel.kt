package com.example.grow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.Makanan
import com.example.grow.data.remote.MakananApiService
import com.example.grow.data.model.MakananInput
import com.example.grow.data.model.AnalisisAsupanRequest
import com.example.grow.data.model.AnalisisAsupanResponse
import com.example.grow.data.remote.AnalisisAsupanApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AsupanViewModel @Inject constructor(
    private val makananApi: MakananApiService,
    private val asupanApi: AnalisisAsupanApiService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _makananGrouped = MutableStateFlow<Map<String, List<Makanan>>>(emptyMap())
    val makananGrouped = _makananGrouped.asStateFlow()

    private val _selectedMakanan = MutableStateFlow<MutableMap<Int, Int>>(mutableMapOf())
    val selectedMakanan = _selectedMakanan.asStateFlow()

    private val _result = MutableStateFlow<AnalisisAsupanResponse?>(null)
    val result = _result.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadMakananIbuHamil() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = makananApi.getMakananIbuHamil()
                val grouped = response.data.groupBy { it.bahan_makanan }
                _makananGrouped.value = grouped
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Gagal memuat data makanan"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun pilihMakanan(idMakanan: Int, jumlahPorsi: Int) {
        _selectedMakanan.value[idMakanan] = jumlahPorsi
    }

    fun kirimAnalisis(idUser: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = _selectedMakanan.value.map { (id, porsi) ->
                    MakananInput(id, porsi)
                }
                val response = asupanApi.analisisAsupan(
                    AnalisisAsupanRequest(
                        id_user = idUser,
                        makanan = list
                    )
                )
                _result.value = response
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Gagal analisis asupan"
            } finally {
                _isLoading.value = false
            }
        }
    }
}