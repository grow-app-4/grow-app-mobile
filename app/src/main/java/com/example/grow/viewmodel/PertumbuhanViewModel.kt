package com.example.grow.ui.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

    private val _latestPertumbuhan = MutableStateFlow<LatestPertumbuhan?>(null)
    val latestPertumbuhan: StateFlow<LatestPertumbuhan?> = _latestPertumbuhan

    private val _children = MutableStateFlow<List<AnakEntity>>(emptyList())
    val children: StateFlow<List<AnakEntity>> = _children

    private val _selectedChildIndex = MutableStateFlow(0)
    val selectedChildIndex: StateFlow<Int> = _selectedChildIndex

    val isLoadingChildren = MutableStateFlow(true)
    val isEmptyChildren = MutableStateFlow(false)

    fun loadChildren(userId: Int) {
        viewModelScope.launch {
            isLoadingChildren.value = true
            try {
                val result = repository.getChildrenByUserId(userId)
                _children.value = result
                isEmptyChildren.value = result.isEmpty()
                _selectedChildIndex.value = 0
            } catch (e: Exception) {
                Log.e("ViewModel", "Error loading children: ${e.message}")
                _children.value = emptyList()
                isEmptyChildren.value = true
            } finally {
                isLoadingChildren.value = false
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
                    idAnak = 0,
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
            Log.d("CREATE_PERTUMBUHAN", "Mulai create pertumbuhan")
            try {
                Log.d("CREATE_PERTUMBUHAN", "Mengirim data ke API: $request")
                val idFromApi = repository.createPertumbuhanToApi(request)

                // Simpan ke lokal dan dapatkan id lokal
                val idLocal = repository.createPertumbuhanToLocal(localEntity, request, localJenis)
                if (idLocal <= 0) {
                    Log.e("CREATE_PERTUMBUHAN", "Gagal menyimpan ke lokal: ID lokal tidak valid ($idLocal)")
                } else if (idFromApi != null) {
                    Log.d("CREATE_PERTUMBUHAN", "Upload ke API sukses, ID dari API: $idFromApi")
                    repository.updateIdApiPertumbuhan(idLocal, idFromApi)
                } else {
                    Log.w("CREATE_PERTUMBUHAN", "Upload ke API gagal (idFromApi = null)")
                }

                // Perbarui data pertumbuhan, data terbaru, dan status stunting
                getPertumbuhanAnak(localEntity.idAnak)
                loadLatestPertumbuhan(localEntity.idAnak)
                loadStatusStunting(localEntity.idAnak)
                _saveSuccess.value = true
            } catch (e: Exception) {
                Log.e("CREATE_PERTUMBUHAN", "Exception saat create pertumbuhan: ${e.message}", e)
                // Simpan ke lokal meskipun API gagal
                val idLocal = repository.createPertumbuhanToLocal(localEntity, request, localJenis)
                if (idLocal <= 0) {
                    Log.e("CREATE_PERTUMBUHAN", "Gagal menyimpan ke lokal di catch block: ID lokal tidak valid ($idLocal)")
                }
                // Perbarui data pertumbuhan, data terbaru, dan status stunting
                getPertumbuhanAnak(localEntity.idAnak)
                loadLatestPertumbuhan(localEntity.idAnak)
                loadStatusStunting(localEntity.idAnak)
                _saveSuccess.value = true
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
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
                    jenisKelamin = jenisKelamin
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
            loadStatusStunting(pertumbuhan.idAnak) // Tambahkan untuk memperbarui AnalysisResultCard
        }
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