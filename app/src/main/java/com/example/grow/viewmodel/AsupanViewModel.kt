package com.example.grow.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.model.Makanan2
import com.example.grow.remote.Makanan2ApiService
import com.example.grow.model.MakananInput
import com.example.grow.model.AnalisisAsupanRequest
import com.example.grow.model.MakananIbu
import com.example.grow.model.NutrisiAnalisisResponse
import com.example.grow.model.StandarNutrisi
import com.example.grow.remote.AsupanApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AsupanViewModel @Inject constructor(
    private val makananApi: Makanan2ApiService,
    private val asupanApi: AsupanApiService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _makananGrouped = MutableStateFlow<Map<String, List<Makanan2>>>(emptyMap())
    val makananGrouped = _makananGrouped.asStateFlow()

    private val _selectedMakanan = MutableStateFlow<MutableMap<Int, Int>>(mutableMapOf())
    val selectedMakanan = _selectedMakanan.asStateFlow()

    private val _result = MutableStateFlow<NutrisiAnalisisResponse?>(null)
    val result = _result.asStateFlow()

    private val _makananIbuData = mutableStateOf<List<MakananIbu>>(emptyList())
    val makananIbuData: State<List<MakananIbu>> = _makananIbuData

    private val _standarNutrisi = mutableStateOf<List<StandarNutrisi>>(emptyList())
    val standarNutrisi: State<List<StandarNutrisi>> = _standarNutrisi

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

    fun fetchMakananIbu(userId: Int) {
        viewModelScope.launch {
            try {
                val response = asupanApi.getMakananIbu(userId)
                _makananIbuData.value = response
            } catch (e: Exception) {
                Log.e("ViewModel", "Gagal ambil data makanan ibu", e)
            }
        }
    }

    fun fetchStandarNutrisi(rentang: String, kategori: String = "ibu_hamil") {
        viewModelScope.launch {
            try {
                val response = asupanApi.getStandarNutrisiByRentang(rentang, kategori)
                _standarNutrisi.value = response
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetch standar nutrisi", e)
            }
        }
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
