package com.example.grow.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.AnakEntity
import com.example.grow.data.repository.AnakRepository
import com.example.grow.data.repository.PertumbuhanRepository
import com.example.grow.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ListDataAnakViewModel @Inject constructor(
    private val anakRepository: AnakRepository,
    private val pertumbuhanRepository: PertumbuhanRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    data class ChildData(
        val anak: AnakEntity,
        val age: String,
        val nutritionStatus: String,
        val stuntingStatus: String,
        val date: String,
        val profileImgUri: String?
    )

    private val _childrenData = MutableStateFlow<List<ChildData>>(emptyList())
    val childrenData: StateFlow<List<ChildData>> = _childrenData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadChildrenData()
    }

    fun loadChildrenData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = SessionManager.getUserId(context)
                // Sinkronkan data dari API
                anakRepository.fetchAllAnakFromApi(context)
                // Ambil data anak berdasarkan userId
                val children = pertumbuhanRepository.getChildrenByUserId(userId)

                // Transformasi data anak menjadi ChildData untuk UI
                val childrenDataList = children.map { anak ->
                    // Hitung usia
                    val age = calculateAge(anak.tanggalLahir)
                    // Ambil status stunting dan tanggal pencatatan terbaru
                    val stuntingStatus = pertumbuhanRepository.getLatestStatusStunting(anak.idAnak) ?: "Tidak Tersedia"
                    val date = pertumbuhanRepository.getTanggalPencatatanTerbaru(anak.idAnak) ?: anak.tanggalLahir
                    // Tentukan status gizi
                    val nutritionStatus = determineNutritionStatus(anak.idAnak)
                    // Bersihkan semua duplikasi /storage/ pada profileImageUri
                    val cleanedProfileImgUri = anak.profileImageUri?.let { cleanStoragePath(it) }

                    ChildData(
                        anak = anak,
                        age = age,
                        nutritionStatus = nutritionStatus,
                        stuntingStatus = stuntingStatus,
                        date = date,
                        profileImgUri = cleanedProfileImgUri
                    )
                }
                _childrenData.value = childrenDataList
            } catch (e: Exception) {
                // Handle error (bisa ditambahkan Snackbar atau Toast di UI)
                _childrenData.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteChild(childId: Int) {
        viewModelScope.launch {
            try {
                // Find the child entity by ID
                val child = _childrenData.value.find { it.anak.idAnak == childId }?.anak
                if (child != null) {
                    // Delete from repository (local and API)
                    anakRepository.deleteAnak(child)
                    // Refresh the data
                    loadChildrenData()
                }
            } catch (e: Exception) {
                // Handle error (e.g., show a Toast or Snackbar in UI)
            }
        }
    }

    private fun calculateAge(tanggalLahir: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val birthDate = LocalDate.parse(tanggalLahir, formatter)
            val currentDate = LocalDate.now()
            val period = Period.between(birthDate, currentDate)
            "${period.years} Tahun ${period.months} Bulan"
        } catch (e: Exception) {
            "Usia Tidak Valid"
        }
    }

    private suspend fun determineNutritionStatus(idAnak: Int): String {
        val idPertumbuhan = pertumbuhanRepository.getIdPertumbuhanTerbaru(idAnak)
            ?: return "Tidak Tersedia"
        val status = pertumbuhanRepository.prosesAnalisisGizi(idPertumbuhan)
        return status ?: "Tidak Diketahui"
    }

    private fun cleanStoragePath(path: String): String {
        var cleanedPath = path
        while (cleanedPath.contains("/storage//storage/")) {
            cleanedPath = cleanedPath.replace("/storage//storage/", "/storage/")
        }
        while (cleanedPath.contains("//storage/")) {
            cleanedPath = cleanedPath.replace("//storage/", "/storage/")
        }
        return cleanedPath
    }
}