package com.example.grow.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.AnakEntity
import com.example.grow.data.DetailPertumbuhanEntity
import com.example.grow.data.JenisPertumbuhanEntity
import com.example.grow.data.LatestPertumbuhan
import com.example.grow.data.PertumbuhanEntity
import com.example.grow.data.PertumbuhanWithDetail
import com.example.grow.data.model.*
import com.example.grow.data.repository.AnakRepository
import com.example.grow.data.repository.PertumbuhanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class PertumbuhanViewModel @Inject constructor(
    private val repository: PertumbuhanRepository,
    private val anakRepository: AnakRepository
) : ViewModel() {

    private val _pertumbuhanList = MutableStateFlow<List<PertumbuhanWithDetail>>(emptyList())
    val pertumbuhanList: StateFlow<List<PertumbuhanWithDetail>> = _pertumbuhanList.asStateFlow()

    private val _statusStunting = MutableStateFlow("Belum ada data analisis")
    val statusStunting: StateFlow<String> = _statusStunting

    private val _latestPertumbuhan = MutableStateFlow<LatestPertumbuhan?>(null)
    val latestPertumbuhan: StateFlow<LatestPertumbuhan?> = _latestPertumbuhan

    private val _children = MutableStateFlow<List<AnakEntity>>(emptyList())
    val children: StateFlow<List<AnakEntity>> = _children

    private val _selectedChildIndex = MutableStateFlow(0)
    val selectedChildIndex: StateFlow<Int> = _selectedChildIndex

    val childAges = MutableStateFlow<Map<Int, String>>(emptyMap())

    fun loadChildren(userId: Int) {
        viewModelScope.launch {
            val anakList = repository.getChildrenByUserId(userId)
            _children.value = anakList
            calculateChildAges(anakList)
        }
    }

    private fun calculateChildAges(children: List<AnakEntity>) {
        val ages = children.associate { anak ->
            anak.idAnak to hitungUmur(anak.tanggalLahir)
        }
        childAges.value = ages
    }

    fun selectChild(index: Int) {
        _selectedChildIndex.value = index
    }

    private fun hitungUmur(tanggalLahir: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val birthDate = LocalDate.parse(tanggalLahir, formatter)
        val currentDate = LocalDate.now()

        val period = Period.between(birthDate, currentDate)

        val tahun = period.years
        val bulan = period.months

        return "$tahun Tahun $bulan Bulan"
    }

    fun loadLatestPertumbuhan(idAnak: Int) {
        viewModelScope.launch {
            val pertumbuhanList = repository.getLatestPertumbuhan(idAnak)
            val tanggal = repository.getTanggalPencatatanTerbaru(idAnak)

            val latestData = LatestPertumbuhan(
                beratBadan = pertumbuhanList.find { it.namaJenis == "Berat Badan" }?.nilai,
                tinggiBadan = pertumbuhanList.find { it.namaJenis == "Tinggi Badan" }?.nilai,
                lingkarKepala = pertumbuhanList.find { it.namaJenis == "Lingkar Kepala" }?.nilai,
                tanggalPencatatan = tanggal
            )

            _latestPertumbuhan.value = latestData

            Log.d("LATEST_PERTUMBUHAN", "Latest data: $latestData")
        }
    }

    fun loadStatusStunting(idAnak: Int) {
        viewModelScope.launch {
            val status = repository.getLatestStatusStunting(idAnak)
            Log.d("STATUS_STUNTING", "Status terbaru yang diambil: $status")
            _statusStunting.value = status ?: "Belum ada data analisis"
        }
    }

    suspend fun getPertumbuhanById(idPertumbuhan: Int): PertumbuhanWithDetail? {
        return repository.getPertumbuhanById(idPertumbuhan)
    }

    fun getPertumbuhanAnak(idAnak: Int) {
        viewModelScope.launch {
            val data = repository.getPertumbuhanByAnak(idAnak)
            _pertumbuhanList.value = data
        }
    }

    fun addAnak(
        nama: String,
        tanggalLahir: String,
        jenisKelamin: String,
        beratBadan: Float,
        tinggiBadan: Float,
        lingkarKepala: Float,
        userId: Int,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val anakEntity = AnakEntity(
                    idAnak = 0, // autoGenerate
                    idUser = userId,
                    namaAnak = nama,
                    tanggalLahir = tanggalLahir,
                    jenisKelamin = jenisKelamin
                )
                anakRepository.addAnakWithInitialGrowth(
                    anakEntity,
                    beratBadan,
                    tinggiBadan,
                    lingkarKepala
                )

                loadChildren(userId)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun createPertumbuhan(
        request: PertumbuhanRequest,
        localEntity: PertumbuhanEntity,
        localJenis: List<JenisPertumbuhanEntity>
    ) {
        viewModelScope.launch {
            try {
                val isSuccess = repository.createPertumbuhanToApi(request)

                if (isSuccess) {
                    Log.d("API_POST", "Upload sukses, insert ke lokal")
                    repository.createPertumbuhanToLocal(localEntity, request, localJenis)
                }
            } catch (e: Exception) {
                Log.e("ERROR", "Gagal create pertumbuhan: ${e.message}")
            }
        }
    }

    fun syncAllFromApi() {
        viewModelScope.launch {
            repository.syncFromApiToRoom()
        }
    }

    fun addPertumbuhan(
        pertumbuhan: PertumbuhanEntity,
        details: List<DetailPertumbuhanEntity>,
        jenisList: List<JenisPertumbuhanEntity>
    ) {
        viewModelScope.launch {
            repository.insertPertumbuhanWithDetails(pertumbuhan, details, jenisList)
            getPertumbuhanAnak(pertumbuhan.idAnak) // refresh data
        }
    }

    // Optional: sinkronisasi dari API
    fun syncFromApi(response: List<com.example.grow.data.model.Pertumbuhan>) {
        viewModelScope.launch {
            repository.syncFromApi(response)
            // misalnya ambil data anak pertama
            val anakId = response.firstOrNull()?.idAnak
            if (anakId != null) getPertumbuhanAnak(anakId)
        }
    }

    fun fetchAndSyncFromApi() {
        viewModelScope.launch {
            repository.syncFromApiToRoom()
            // optionally update local cache
            val idAnak = repository.getAllPertumbuhan().firstOrNull()?.pertumbuhan?.idAnak
            if (idAnak != null) getPertumbuhanAnak(idAnak)
        }
    }

    fun updatePertumbuhan(
        pertumbuhan: PertumbuhanEntity,
        details: List<DetailPertumbuhanEntity>
    ) {
        viewModelScope.launch {
            repository.updatePertumbuhanWithDetails(pertumbuhan, details)
            getPertumbuhanAnak(pertumbuhan.idAnak)
        }
    }

    fun deletePertumbuhan(idPertumbuhan: Int, idAnak: Int) {
        viewModelScope.launch {
            repository.deletePertumbuhan(idPertumbuhan)
            getPertumbuhanAnak(idAnak)
        }
    }

}