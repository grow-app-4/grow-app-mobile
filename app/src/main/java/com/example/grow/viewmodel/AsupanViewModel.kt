package com.example.grow.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.Makanan
import com.example.grow.data.remote.MakananApiService
import com.example.grow.data.model.MakananInput
import com.example.grow.data.model.AnalisisAsupanRequest
import com.example.grow.data.model.AnalisisData
import com.example.grow.data.model.AsupanAsi
import com.example.grow.data.model.MakananIbu
import com.example.grow.data.model.NutrisiAnalisisResponse
import com.example.grow.data.model.StandarNutrisi
import com.example.grow.data.remote.AsupanApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class AsupanViewModel @Inject constructor(
    private val makananApi: MakananApiService,
    private val asupanApi: AsupanApiService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _makananGrouped = MutableStateFlow<Map<String, List<Makanan>>>(emptyMap())
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

    private val _asupanHariIni = MutableStateFlow<Boolean?>(null)
    val asupanHariIni: StateFlow<Boolean?> = _asupanHariIni

    private val _tanggalDipilih = MutableStateFlow<String?>(null)
    val tanggalDipilih: StateFlow<String?> = _tanggalDipilih

    private val _jumlahPorsi = MutableStateFlow("")
    val jumlahPorsi: StateFlow<String> = _jumlahPorsi

    private val _tanggalKonsumsi = MutableStateFlow("") // format yyyy-MM-dd
    val tanggalKonsumsi: StateFlow<String> = _tanggalKonsumsi

    private val _hasilAnalisis = MutableStateFlow<String?>(null)
    val hasilAnalisis: StateFlow<String?> = _hasilAnalisis

    private val _dataAnalisis = MutableStateFlow<AnalisisData?>(null)
    val dataAnalisis: StateFlow<AnalisisData?> = _dataAnalisis

    private val _dataSudahAda = MutableStateFlow(false)
    val dataSudahAda: StateFlow<Boolean> = _dataSudahAda

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _usiaAnak = mutableStateOf("")
    val usiaAnak: State<String> = _usiaAnak

    fun setTanggalDipilih(tanggal: String) {
        _tanggalDipilih.value = tanggal
    }

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

    fun checkAsupanHariIni(userId: Int, tanggal: String) {
        viewModelScope.launch {
            try {
                val response = asupanApi.checkAsupan(userId, tanggal)
                _asupanHariIni.value = response.status
                _makananIbuData.value = response.data
            } catch (e: Exception) {
                Log.e("ViewModel", "Gagal cek asupan: ${e.message}")
                _asupanHariIni.value = false // fallback jika gagal
            }
        }
    }

    fun pilihMakanan(idMakanan: Int, jumlahPorsi: Int) {
        _selectedMakanan.value[idMakanan] = jumlahPorsi
    }

    fun fetchMakananIbu(userId: Int, tanggal: String) {
        viewModelScope.launch {
            try {
                val response = asupanApi.getMakananIbu(userId, tanggal)
                _makananIbuData.value = response
            } catch (e: Exception) {
                Log.e("ViewModel", "Gagal ambil data makanan ibu pada tanggal $tanggal", e)
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

    fun kirimAnalisis(idUser: Int, tanggalKonsumsi: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = _selectedMakanan.value.map { (id, porsi) ->
                    MakananInput(id_makanan = id, jumlah_porsi = porsi)
                }
                val response = asupanApi.analisisAsupan(
                    AnalisisAsupanRequest(
                        id_user = idUser,
                        tanggal_konsumsi = tanggalKonsumsi,
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

    //anak
    fun setJumlahPorsi(value: String) {
        _jumlahPorsi.value = value
    }

    fun setTanggalKonsumsi(value: String) {
        _tanggalKonsumsi.value = value
    }

    fun inputAsupan(idAnak: Int) {
        val jumlah = jumlahPorsi.value.toIntOrNull()
        val tanggal = tanggalKonsumsi.value

        if (jumlah == null || jumlah <= 0) {
            _error.value = "Jumlah porsi harus berupa angka positif"
            return
        }
        if (tanggal.isBlank()) {
            _error.value = "Tanggal konsumsi harus diisi"
            return
        }

        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = asupanApi.inputAsupanAsi(
                    AsupanAsi(
                        idAnak = idAnak,
                        tanggalKonsumsi = tanggal,
                        jumlahPorsiDikonsumsi = jumlah
                    )
                )
                if (response.isSuccessful) {
                    // Ambil hasil analisis terbaru dari backend (jika perlu)
                    val hasilResponse = asupanApi.getAsupanByAnakAndTanggal(idAnak, tanggal)
                    if (hasilResponse.isSuccessful) {
                        _hasilAnalisis.value = hasilResponse.body()?.hasilAnalisis
                    }
                    getAsupanAnakByIdAnakAndTanggal(idAnak, _tanggalKonsumsi.value)
                } else {
                    _error.value = "Gagal input data: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun getAsupanAnakByIdAnakAndTanggal(idAnak: Int, tanggal: String) {
        _dataSudahAda.value = false
        viewModelScope.launch {
            try {
                val response = asupanApi.getAsupanByAnakAndTanggal(idAnak, tanggal)
                if (response.isSuccessful) {
                    val asupan = response.body()
                    if (asupan != null) {
                        _loading.value = false
                        _dataSudahAda.value = true
                        _usiaAnak.value = asupan.usiaAnak
                        _hasilAnalisis.value = asupan.hasilAnalisis
                        _dataAnalisis.value = parseHasilAnalisisString(
                            hasilAnalisis = asupan.hasilAnalisis,
                            jumlahPorsi = asupan.jumlahPorsiDikonsumsi
                        )
                    } else {
                        _loading.value = false
                        _dataSudahAda.value = false
                        _hasilAnalisis.value = null
                        _dataAnalisis.value = null
                    }
                } else {
                    _loading.value = false
                    _dataSudahAda.value = false
                    _hasilAnalisis.value = null
                    _dataAnalisis.value = null
                    Log.e("AsupanViewModel", "Gagal getAsupan: ${response.code()}")
                }
            } catch (e: Exception) {
                _dataSudahAda.value = false
                _hasilAnalisis.value = null
                _dataAnalisis.value = null
                Log.e("AsupanViewModel", "Exception getAsupan: ${e.message}")
            }
        }
    }

    private fun parseHasilAnalisisString(hasilAnalisis: String?, jumlahPorsi: Int): AnalisisData? {
        if (hasilAnalisis == null) return null

        val regex = Regex("""Standar:\s*(\d+),\s*hasil:\s*(\w+)""")
        val matchResult = regex.find(hasilAnalisis)

        return matchResult?.let {
            val standar = it.groupValues[1].toInt()
            val status = it.groupValues[2]
            AnalisisData(
                standarFrekuensi = standar,
                statusFrekuensi = status,
                jumlahPorsiDikonsumsi = jumlahPorsi
            )
        }
    }

    fun initTanggalHariIniJikaKosong() {
        if (_tanggalKonsumsi.value.isBlank()) {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = formatter.format(Date())
            _tanggalKonsumsi.value = today
        }
    }
}