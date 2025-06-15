package com.example.grow.ui.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class PertumbuhanViewModel @Inject constructor(
    private val repository: PertumbuhanRepository,
    private val anakRepository: AnakRepository
) : ViewModel() {

    private val _pertumbuhanList = MutableStateFlow<List<PertumbuhanWithDetail>>(emptyList())
    val pertumbuhanList: StateFlow<List<PertumbuhanWithDetail>> = _pertumbuhanList.asStateFlow()

    private val _statusStunting = MutableStateFlow("Belum ada data analisis")
    val statusStunting: StateFlow<String> = _statusStunting

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _latestPertumbuhan = MutableStateFlow<LatestPertumbuhan?>(null)
    val latestPertumbuhan: StateFlow<LatestPertumbuhan?> = _latestPertumbuhan

    private val _children = MutableStateFlow<List<AnakEntity>>(emptyList())
    val children: StateFlow<List<AnakEntity>> = _children

    private val _selectedChildIndex = MutableStateFlow(0)
    val selectedChildIndex: StateFlow<Int> = _selectedChildIndex

    val isLoadingChildren = MutableStateFlow(true)
    val isEmptyChildren = MutableStateFlow(false)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val _addAnakStatus = MutableStateFlow<AddAnakStatus>(AddAnakStatus.Idle)
    val addAnakStatus: StateFlow<AddAnakStatus> = _addAnakStatus.asStateFlow()

    sealed class AddAnakStatus {
        object Idle : AddAnakStatus()
        data class Success(val idAnak: Int) : AddAnakStatus() // Simpan idAnak
        data class Error(val message: String) : AddAnakStatus()
    }

    fun loadChildren(userId: Int) {
        viewModelScope.launch {
            isLoadingChildren.value = true
            try {
                val result = repository.getChildrenByUserId(userId)
                _children.value = result
                isEmptyChildren.value = result.isEmpty()
                _selectedChildIndex.value = 0
                calculateChildAges(result)
            } catch (e: Exception) {
                Log.e("ViewModel", "Error loading children: ${e.message}", e)
                _children.value = emptyList()
                isEmptyChildren.value = true
                _errorMessage.value = "Gagal memuat data anak: ${e.message}"
            } finally {
                isLoadingChildren.value = false
            }
        }
    }

    fun syncPertumbuhan(userId: Int) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            try {
                repository.syncPertumbuhanByUserId(userId)
                loadChildren(userId)
                Log.d("PertumbuhanViewModel", "Sinkronisasi berhasil untuk userId: $userId")
            } catch (e: Exception) {
                Log.e("PertumbuhanViewModel", "Gagal sinkronisasi: ${e.message}", e)
                _syncError.value = "Gagal menyinkronkan data: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    val selectedChild: StateFlow<AnakEntity?> = combine(children, selectedChildIndex) { anakList, index ->
        anakList.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _syncSuccess = MutableLiveData<Boolean>()
    val syncSuccess: LiveData<Boolean> = _syncSuccess

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun loadDataAwal() {
        viewModelScope.launch {
            try {
                repository.syncJenisPertumbuhan()
                _syncSuccess.value = true
                Log.d("PertumbuhanViewModel", "Sync jenis pertumbuhan selesai")
            } catch (e: CancellationException) {
                Log.d("PertumbuhanViewModel", "Sync dibatalkan: ${e.message}")
            } catch (e: Exception) {
                Log.e("PertumbuhanViewModel", "Error fetching jenis data: ${e.message}")
            }
        }
    }

    val childAges = MutableStateFlow<Map<Int, String>>(emptyMap())

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
        val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val simpleFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val birthDate = try {
            // Try parsing ISO 8601 format first (e.g., 2025-06-15T00:00:00.000000Z)
            LocalDate.parse(tanggalLahir, isoFormatter)
        } catch (e: DateTimeParseException) {
            try {
                // Fallback to simple yyyy-MM-dd format
                LocalDate.parse(tanggalLahir, simpleFormatter)
            } catch (e2: DateTimeParseException) {
                Log.e("ViewModel", "Invalid date format for tanggalLahir: $tanggalLahir", e2)
                return "Umur tidak valid"
            }
        }
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
        userId: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _errorMessage.value = null
            _addAnakStatus.value = AddAnakStatus.Idle
            try {
                // Simpan anak
                val anakEntity = AnakEntity(
                    idAnak = 0,
                    idUser = userId,
                    namaAnak = nama,
                    tanggalLahir = tanggalLahir,
                    jenisKelamin = jenisKelamin,
                    profileImageUri = null
                )
                val anakId = anakRepository.addAnakWithInitialGrowth(anakEntity)
                Log.d("ADD_ANAK", "Anak disimpan dengan ID: $anakId")

                // Buat PertumbuhanRequest
                val tanggalPencatatan = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                val pertumbuhanRequest = PertumbuhanRequest(
                    idAnak = anakId,
                    tanggalPencatatan = tanggalPencatatan,
                    statusStunting = "",
                    details = listOf(
                        DetailRequest(idJenis = 1, nilai = tinggiBadan),
                        DetailRequest(idJenis = 2, nilai = beratBadan),
                        DetailRequest(idJenis = 3, nilai = lingkarKepala)
                    )
                )

                // Buat PertumbuhanEntity
                val pertumbuhanEntity = PertumbuhanEntity(
                    idPertumbuhan = 0,
                    idAnak = anakId,
                    tanggalPencatatan = tanggalPencatatan,
                    statusStunting = ""
                )

                // Kirim ke API dan hitung stunting
                val (idFromApi, error) = repository.createPertumbuhanToApi(pertumbuhanRequest)
                if (error != null) {
                    _errorMessage.value = error
                    throw Exception(error)
                }

                // Simpan ke lokal
                val localJenis = repository.getJenisPertumbuhan()
                val idLocal = repository.createPertumbuhanToLocal(pertumbuhanEntity, pertumbuhanRequest, localJenis)
                if (idLocal <= 0) {
                    throw Exception("Gagal menyimpan data pertumbuhan lokal")
                }

                if (idFromApi != null) {
                    repository.updateIdApiPertumbuhan(idLocal, idFromApi)
                }

                // Perbarui UI
                loadChildren(userId)
                getPertumbuhanAnak(anakId)
                loadLatestPertumbuhan(anakId)
                loadStatusStunting(anakId)
                _saveSuccess.value = true
                _addAnakStatus.value = AddAnakStatus.Success(anakId)
            } catch (e: Exception) {
                Log.e("ADD_ANAK", "Error: ${e.message}", e)
                _errorMessage.value = "Gagal menambahkan anak: ${e.message}"
                _addAnakStatus.value = AddAnakStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetAddAnakStatus() {
        _addAnakStatus.value = AddAnakStatus.Idle
    }

    fun createPertumbuhan(
        request: PertumbuhanRequest,
        localEntity: PertumbuhanEntity,
        localJenis: List<JenisPertumbuhanEntity>
    ) {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                Log.d("CREATE_PERTUMBUHAN", "Mengirim data ke API: $request")
                val (idFromApi, error) = repository.createPertumbuhanToApi(request)

                if (error != null) {
                    _errorMessage.value = error
                }

                // Simpan ke lokal
                val idLocal = repository.createPertumbuhanToLocal(localEntity, request, localJenis)
                if (idLocal <= 0) {
                    Log.e("CREATE_PERTUMBUHAN", "Gagal simpan lokal: ID $idLocal")
                    _errorMessage.value = "Gagal menyimpan data lokal"
                    return@launch
                }

                if (idFromApi != null) {
                    repository.updateIdApiPertumbuhan(idLocal, idFromApi)
                }

                getPertumbuhanAnak(localEntity.idAnak)
                loadLatestPertumbuhan(localEntity.idAnak)
                loadStatusStunting(localEntity.idAnak)
                _saveSuccess.value = true
            } catch (e: Exception) {
                Log.e("CREATE_PERTUMBUHAN", "Gagal create: ${e.message}", e)
                val idLocal = repository.createPertumbuhanToLocal(localEntity, request, localJenis)
                if (idLocal <= 0) {
                    _errorMessage.value = "Gagal menyimpan data lokal"
                } else {
                    getPertumbuhanAnak(localEntity.idAnak)
                    loadLatestPertumbuhan(localEntity.idAnak)
                    loadStatusStunting(localEntity.idAnak)
                    _saveSuccess.value = true
                }
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
        _errorMessage.value = null
    }

    fun getAnakById(
        anakId: Int,
        userId: Int,
        context: Context,
        onSuccess: (AnakEntity) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val anak = anakRepository.getAnakById(anakId).firstOrNull()
                if (anak != null && anak.idUser == userId) {
                    Log.d("PertumbuhanViewModel", "Anak ditemukan: ${anak.namaAnak}")
                    onSuccess(anak)
                } else {
                    throw Exception("Anak dengan ID $anakId tidak ditemukan atau tidak terkait dengan user")
                }
            } catch (e: Exception) {
                Log.e("PertumbuhanViewModel", "Error mengambil anak: ${e.message}", e)
                onError(e)
            }
        }
    }

    fun updateAnak(
        anakId: Int,
        nama: String,
        tanggalLahir: String,
        jenisKelamin: String,
        userId: Int,
        context: Context,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!isValidInput(nama, tanggalLahir, jenisKelamin)) {
                    throw IllegalArgumentException("Data tidak valid. Pastikan semua field diisi dengan benar.")
                }
                val isConnected = isNetworkConnected(context)
                val anakEntity = AnakEntity(
                    idAnak = anakId,
                    idUser = userId,
                    namaAnak = nama,
                    tanggalLahir = tanggalLahir,
                    jenisKelamin = jenisKelamin,
                    profileImageUri = null
                )
                anakRepository.updateAnak(anakEntity)
                loadChildren(userId)
                onSuccess()
            } catch (e: Exception) {
                Log.e("PertumbuhanViewModel", "Error memperbarui anak: ${e.message}", e)
                onError(e)
            }
        }
    }

    private fun isValidInput(
        nama: String,
        tanggalLahir: String,
        jenisKelamin: String
    ): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val birthDate = try {
            LocalDate.parse(tanggalLahir, formatter)
        } catch (e: Exception) {
            return false
        }
        return nama.isNotBlank() &&
                tanggalLahir.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) &&
                jenisKelamin in listOf("L", "P") &&
                birthDate.isBefore(LocalDate.now())
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun updatePertumbuhan(
        pertumbuhan: PertumbuhanEntity,
        details: List<DetailPertumbuhanEntity>
    ) {
        viewModelScope.launch {
            repository.updatePertumbuhanWithDetails(pertumbuhan, details)
            getPertumbuhanAnak(pertumbuhan.idAnak)
            loadLatestPertumbuhan(pertumbuhan.idAnak)
            loadStatusStunting(pertumbuhan.idAnak)
        }
    }

    fun updateProfileImageUri(anakId: Int, uri: Uri?) {
        // Implementasi sementara untuk menyimpan URI di ViewModel atau langsung ke repository
        // Anda mungkin ingin menyimpan ini ke state sementara atau langsung ke database
    }

    fun deletePertumbuhan(idPertumbuhan: Int, idAnak: Int) {
        viewModelScope.launch {
            repository.deletePertumbuhan(idPertumbuhan)
            getPertumbuhanAnak(idAnak) // Untuk memperbarui GrowthHistoryTable
            loadLatestPertumbuhan(idAnak) // Untuk memperbarui GrowthDataCard
            loadStatusStunting(idAnak) // Untuk memperbarui AnalysisResultCard
        }
    }
}