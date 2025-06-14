package com.example.grow.data.repository

import android.util.Log
import com.example.grow.data.AnakDao
import com.example.grow.data.AnakEntity
import com.example.grow.data.DetailPertumbuhanDao
import com.example.grow.data.PertumbuhanEntity
import com.example.grow.data.DetailPertumbuhanEntity
import com.example.grow.data.GrowthData
import com.example.grow.data.JenisPertumbuhanDao
import com.example.grow.data.PertumbuhanDao
import com.example.grow.data.JenisPertumbuhanEntity
import com.example.grow.data.PertumbuhanWithDetail
import com.example.grow.data.StandarPertumbuhanDao
import com.example.grow.data.StandarPertumbuhanEntity
import com.example.grow.data.api.PertumbuhanApiService
import com.example.grow.data.model.Pertumbuhan
import com.example.grow.data.model.PertumbuhanRequest
import com.example.grow.data.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class PertumbuhanRepository @Inject constructor(
    private val anakDao: AnakDao,
    private val pertumbuhanDao: PertumbuhanDao,
    private val detailDao: DetailPertumbuhanDao,
    private val jenisPertumbuhanDao: JenisPertumbuhanDao,
    private val standarDao: StandarPertumbuhanDao,
    private val apiService: PertumbuhanApiService
) {

    suspend fun getAllPertumbuhan(): List<PertumbuhanWithDetail> {
        return pertumbuhanDao.getAllPertumbuhan()
    }

    suspend fun getPertumbuhanById(idPertumbuhan: Int): PertumbuhanWithDetail? {
        return pertumbuhanDao.getPertumbuhanById(idPertumbuhan)
    }

    suspend fun getPertumbuhanByAnak(idAnak: Int): List<PertumbuhanWithDetail> {
        return pertumbuhanDao.getPertumbuhanByAnak(idAnak)
    }

    suspend fun syncJenisPertumbuhan() = withContext(Dispatchers.IO) {
        val localData = jenisPertumbuhanDao.getAllJenisPertumbuhan()

        if (localData.isEmpty()) {
            try {
                val response = apiService.getJenisPertumbuhan()

                if (response.isSuccessful) {
                    response.body()?.data?.let { jenisList ->
                        val entities = jenisList.map {
                            JenisPertumbuhanEntity(
                                idJenis = it.idJenis,
                                namaJenis = it.namaJenis
                            )
                        }
                        jenisPertumbuhanDao.insertAllJenis(entities)
                        Log.d("SYNC_JENIS", "Data dari API berhasil disimpan (${entities.size} item)")
                    }
                } else {
                    Log.w("SYNC_JENIS", "Response API tidak sukses: ${response.message()}")
                    insertDefaultJenisIfNeeded()
                }
            } catch (e: Exception) {
                Log.e("SYNC_JENIS", "Gagal ambil data dari API: ${e.message}")
                insertDefaultJenisIfNeeded()
            }
        } else {
            Log.d("SYNC_JENIS", "Data jenis pertumbuhan sudah tersedia di lokal.")
        }
    }

    private suspend fun insertDefaultJenisIfNeeded() {
        val hardcodedJenis = listOf(
            JenisPertumbuhanEntity(idJenis = 1, namaJenis = "Tinggi Badan"),
            JenisPertumbuhanEntity(idJenis = 2, namaJenis = "Berat Badan"),
            JenisPertumbuhanEntity(idJenis = 3, namaJenis = "Lingkar Kepala")
        )

        jenisPertumbuhanDao.insertAllJenis(hardcodedJenis)
        Log.w("SYNC_JENIS", "🟡 Data lokal kosong, menggunakan data default sebanyak ${hardcodedJenis.size}")
    }

    fun getAllJenisFlow() = jenisPertumbuhanDao.getAllJenis()

    suspend fun insertPertumbuhanWithDetails(
        pertumbuhan: PertumbuhanEntity,
        details: List<DetailPertumbuhanEntity>,
        jenisList: List<JenisPertumbuhanEntity>
    ) {
        val pertumbuhanId = pertumbuhanDao.insertPertumbuhan(pertumbuhan).toInt()
        Log.d("DEBUG_PERTUMBUHAN", "Inserted pertumbuhan: $pertumbuhanId")
        val detailWithId = details.map { it.copy(idPertumbuhan = pertumbuhanId) }
        pertumbuhanDao.insertDetailPertumbuhan(detailWithId)
        Log.d("DEBUG_DETAIL", "Inserted details: $detailWithId")
    }

    suspend fun createPertumbuhanToLocal(
        entity: PertumbuhanEntity,
        request: PertumbuhanRequest,
        localJenis: List<JenisPertumbuhanEntity>
    ): Int {
        Log.d("LOCAL_INSERT", "Memulai penyimpanan data pertumbuhan ke lokal...")

        Log.d("LOCAL_INSERT", "Menyimpan entitas pertumbuhan: $entity")
        val idPertumbuhan = pertumbuhanDao.insertPertumbuhan(entity).toInt()
        Log.d("LOCAL_INSERT", "ID pertumbuhan yang di-generate: $idPertumbuhan")

        val detailEntities = request.details.map { detail ->
            Log.d("LOCAL_INSERT", "Mapping detail: idJenis=${detail.idJenis}, nilai=${detail.nilai}")
            DetailPertumbuhanEntity(
                idPertumbuhan = idPertumbuhan,
                idJenis = detail.idJenis,
                nilai = detail.nilai
            )
        }

        Log.d("LOCAL_INSERT", "Menyimpan ${detailEntities.size} detail pertumbuhan")
        pertumbuhanDao.insertDetailPertumbuhan(detailEntities)

        Log.d("LOCAL_INSERT", "Memulai proses analisis stunting untuk ID: $idPertumbuhan")
        prosesAnalisisStunting(idPertumbuhan)

        Log.d("LOCAL_INSERT", "Data pertumbuhan dan detail berhasil disimpan lokal")
        return idPertumbuhan
    }

    suspend fun updatePertumbuhanWithDetails(
        pertumbuhan: PertumbuhanEntity,
        details: List<DetailPertumbuhanEntity>
    ) {
        pertumbuhanDao.updatePertumbuhan(pertumbuhan)
        pertumbuhanDao.deleteDetailsByPertumbuhanId(pertumbuhan.idPertumbuhan)
        pertumbuhanDao.insertDetailPertumbuhan(details)
        prosesAnalisisStunting(pertumbuhan.idPertumbuhan)
    }

    suspend fun createPertumbuhanToApi(
        request: PertumbuhanRequest
    ): Pair<Int?, String?> {
        try {
            Log.d("API_POST", "Memulai pengiriman data: $request")

            // Validasi id_anak
            val anak = anakDao.getAnakId(request.idAnak)
                ?: run {
                    Log.w("API_POST", "Anak tidak ditemukan untuk idAnak: ${request.idAnak}")
                    return Pair(null, "ID anak tidak valid")
                }

            // Ubah format tanggal
            val formatterCatat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val formatterOutput = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val tanggalFormatted = try {
                LocalDate.parse(request.tanggalPencatatan, formatterCatat)
                    .format(formatterOutput)
            } catch (e: DateTimeParseException) {
                Log.w("API_POST", "Gagal parsing tanggal: ${e.message}")
                return Pair(null, "Format tanggal tidak valid")
            }

            // Hitung status stunting
            val status = calculateStuntingStatus(
                idAnak = request.idAnak,
                tanggalPencatatan = tanggalFormatted,
                tinggiBadan = request.details.firstOrNull { it.idJenis == JENIS_TINGGI_BADAN }?.nilai
            ) ?: run {
                Log.w("API_POST", "Gagal menghitung status stunting")
                return Pair(null, "Gagal menghitung status stunting")
            }

            // Buat request baru dengan data yang benar
            val updatedRequest = request.copy(
                tanggalPencatatan = tanggalFormatted,
                statusStunting = status
            )

            // Kirim ke API
            val response = apiService.createPertumbuhan(updatedRequest)
            if (response.isSuccessful) {
                val idFromApi = response.body()?.data?.idPertumbuhan
                Log.d("API_POST", "Sukses kirim ke API. ID: $idFromApi, Status: $status")
                return Pair(idFromApi, null)
            } else {
                Log.e("API_POST", "Gagal kirim ke API: ${response.code()} - ${response.message()}")
                val errorMessage = when (response.code()) {
                    422 -> {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_POST", "Detail error validasi: $errorBody")
                        errorBody?.let { parseValidationError(it) } ?: "Validasi gagal, periksa data input"
                    }
                    404 -> "Endpoint tidak ditemukan. Periksa URL atau server"
                    else -> "Gagal menyimpan data ke server"
                }
                return Pair(null, errorMessage)
            }
        } catch (e: Exception) {
            Log.e("API_POST", "Error saat kirim ke API: ${e.message}", e)
            return Pair(null, "Terjadi kesalahan: ${e.message}")
        }
    }

    private suspend fun calculateStuntingStatus(
        idAnak: Int,
        tanggalPencatatan: String,
        tinggiBadan: Float?
    ): String? {
        if (tinggiBadan == null) return null

        try {
            // Ambil data anak
            val anak = anakDao.getAnakId(idAnak) ?: return null

            // Hitung usia dalam bulan
            val formatterLahir = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formatterCatat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val usiaBulan = try {
                ChronoUnit.MONTHS.between(
                    LocalDate.parse(anak.tanggalLahir, formatterLahir),
                    LocalDate.parse(tanggalPencatatan, formatterCatat)
                ).toInt()
            } catch (e: DateTimeParseException) {
                Log.w("CALC_STUNTING", "Gagal parsing tanggal: ${e.message}")
                return null
            }

            // Validasi usia
            if (usiaBulan < 0 || usiaBulan > 60) {
                Log.w("CALC_STUNTING", "Usia tidak valid: $usiaBulan bulan")
                return null
            }

            // Validasi tinggi badan
            if (tinggiBadan < 30f || tinggiBadan > 150f) {
                Log.w("CALC_STUNTING", "Tinggi badan tidak realistis: $tinggiBadan")
                return null
            }

            // Ambil data standar
            val standarList = standarDao.getStandarByUsiaAndJenisKelamin(
                usiaBulan, anak.jenisKelamin, JENIS_TINGGI_BADAN
            )
            if (standarList.isEmpty()) {
                Log.w("CALC_STUNTING", "Data standar tidak ditemukan")
                return null
            }

            // Cari median (z_score = 0)
            val median = standarList.find { it.z_score == 0f }?.nilai ?: return null

            // Hitung standar deviasi
            val zScorePlusOne = standarList.find { it.z_score == 1f }?.nilai
            val standarDeviasi = zScorePlusOne?.let { it - median } ?: 3.0f

            if (standarDeviasi == 0f) {
                Log.w("CALC_STUNTING", "Standar deviasi nol")
                return null
            }

            // Hitung z-score
            val zScore = (tinggiBadan - median) / standarDeviasi
            return mapZScoreToStatus(zScore)
        } catch (e: Exception) {
            Log.e("CALC_STUNTING", "Error menghitung status: ${e.message}", e)
            return null
        }
    }

    private fun parseValidationError(errorBody: String): String {
        return try {
            val json = JSONObject(errorBody)
            val errors = json.getJSONObject("errors")
            val errorMessages = mutableListOf<String>()
            errors.keys().forEach { key ->
                val messages = errors.getJSONArray(key)
                for (i in 0 until messages.length()) {
                    errorMessages.add(messages.getString(i))
                }
            }
            errorMessages.joinToString("; ")
        } catch (e: Exception) {
            "Gagal memproses error validasi"
        }
    }

    suspend fun updateIdApiPertumbuhan(idLocal: Int, idApi: Int) {
        try {
            pertumbuhanDao.updateIdApiPertumbuhan(idLocal, idApi)
            Log.d("REPOSITORY", "Berhasil update idApiPertumbuhan: idLocal=$idLocal, idApi=$idApi")
        } catch (e: Exception) {
            Log.e("REPOSITORY", "Gagal update idApiPertumbuhan: ${e.message}", e)
        }
    }

    private suspend fun prosesAnalisisStunting(idPertumbuhan: Int) {
        try {
            Log.d("AnalisisStunting", "Mulai analisis untuk idPertumbuhan: $idPertumbuhan")

            // Ambil data pertumbuhan
            val pertumbuhan = pertumbuhanDao.getPertumbuhanId(idPertumbuhan)
                ?: run {
                    Log.w("AnalisisStunting", "Data pertumbuhan tidak ditemukan")
                    return
                }

            // Ambil data anak
            val anak = anakDao.getAnakId(pertumbuhan.idAnak)
                ?: run {
                    Log.w("AnalisisStunting", "Data anak tidak ditemukan")
                    return
                }

            // Ambil detail tinggi badan
            val detailList = detailDao.getDetailByPertumbuhanId(idPertumbuhan)
            val detailTinggi = detailList.firstOrNull { it.idJenis == JENIS_TINGGI_BADAN }
                ?: run {
                    Log.w("AnalisisStunting", "Detail tinggi badan tidak ditemukan")
                    return
                }

            // Hitung usia dalam bulan
            val formatterLahir = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Sesuaikan dengan format tanggal_lahir
            val formatterCatat = DateTimeFormatter.ofPattern("dd-MM-yyyy") // Sesuaikan dengan format tanggal_pencatatan
            val usiaBulan = try {
                ChronoUnit.MONTHS.between(
                    LocalDate.parse(anak.tanggalLahir, formatterLahir),
                    LocalDate.parse(pertumbuhan.tanggalPencatatan, formatterCatat)
                ).toInt()
            } catch (e: DateTimeParseException) {
                Log.w("AnalisisStunting", "Gagal parsing tanggal: ${e.message}")
                return
            }

            // Ambil daftar standar untuk usia, jenis kelamin, dan jenis tinggi badan
            val standarList = standarDao.getStandarByUsiaAndJenisKelamin(
                usiaBulan, anak.jenisKelamin, JENIS_TINGGI_BADAN
            )
            if (standarList.isEmpty()) {
                Log.w("AnalisisStunting", "Data standar tidak ditemukan untuk usia $usiaBulan, jenis kelamin ${anak.jenisKelamin}")
                return
            }

            // Cari median (z_score = 0)
            val median = standarList.find { it.z_score == 0f }?.nilai
                ?: run {
                    Log.w("AnalisisStunting", "Median (z_score = 0) tidak ditemukan")
                    return
                }

            // Hitung standar deviasi (selisih antara z_score 0 dan 1)
            val zScorePlusOne = standarList.find { it.z_score == 1f }?.nilai
            val standarDeviasi = if (zScorePlusOne != null) {
                zScorePlusOne - median // Misalnya, 85.7 - 82.7 = 3.0 cm
            } else {
                Log.w("AnalisisStunting", "Standar deviasi tidak dapat dihitung, menggunakan default 3.0 cm")
                3.0f // Fallback jika z_score = 1 tidak tersedia
            }

            // Pastikan standar deviasi tidak nol
            if (standarDeviasi == 0f) {
                Log.w("AnalisisStunting", "Standar deviasi nol, z-score tidak dapat dihitung")
                return
            }

            // Hitung z-score sesuai WHO
            val zScore = (detailTinggi.nilai - median) / standarDeviasi
            Log.d("AnalisisStunting", "Z-Score: $zScore (tinggi: ${detailTinggi.nilai}, median: $median, stdDev: $standarDeviasi)")

            // Tentukan status sesuai WHO
            val status = mapZScoreToStatus(zScore)
            Log.d("AnalisisStunting", "Status: $status")

            // Perbarui status di database
            val rowsAffected = pertumbuhanDao.updateStatusStunting(idPertumbuhan, status)
            Log.d("AnalisisStunting", "Berhasil update status stunting")
        } catch (e: Exception) {
            Log.e("AnalisisStunting", "Terjadi error saat analisis", e)
        }
    }

    private fun mapZScoreToStatus(zScore: Float): String {
        return when {
            zScore < -3 -> "Sangat Pendek"
            zScore < -2 -> "Pendek"
            zScore > 3 -> "Sangat Tinggi"
            zScore > 2 -> "Tinggi di Atas Rata-Rata"
            else -> "Normal"
        }
    }

    companion object {
        const val JENIS_TINGGI_BADAN = 1
    }

    suspend fun getLatestPertumbuhan(idAnak: Int): List<GrowthData> {
        return pertumbuhanDao.getLatestPertumbuhanByIdAnak(idAnak)
    }

    suspend fun getLatestStatusStunting(idAnak: Int): String? {
        return pertumbuhanDao.getLatestStatusStunting(idAnak)
    }

    suspend fun getTanggalPencatatanTerbaru(idAnak: Int): String? {
        return pertumbuhanDao.getTanggalPencatatanTerbaru(idAnak)
    }

    suspend fun getChildrenByUserId(userId: Int): List<AnakEntity> {
        return anakDao.getChildrenByUserId(userId)
    }

    suspend fun fetchPertumbuhanFromApi(): List<Pertumbuhan> {
        val response = apiService.getPertumbuhan()
        return response.data
    }

    suspend fun syncFromApiToRoom() {
        val apiData = fetchPertumbuhanFromApi()
        syncFromApi(apiData)
    }

    suspend fun syncFromApi(responseList: List<Pertumbuhan>) {
        for (item in responseList) {
            val pertumbuhanEntity = PertumbuhanEntity(
                idPertumbuhan = item.idPertumbuhan,
                idAnak = item.idAnak,
                tanggalPencatatan = item.tanggalPencatatan,
                statusStunting = item.statusStunting
            )

            val jenisList = item.detailPertumbuhan.map {
                JenisPertumbuhanEntity(it.jenis.idJenis, it.jenis.namaJenis)
            }.distinctBy { it.idJenis }

            val detailList = item.detailPertumbuhan.map {
                DetailPertumbuhanEntity(
                    idPertumbuhan = it.idPertumbuhan,
                    idJenis = it.idJenis,
                    nilai = it.nilai
                )
            }

            insertPertumbuhanWithDetails(pertumbuhanEntity, detailList, jenisList)
        }
    }

    fun getPertumbuhanAnak(idAnak: Int): Flow<List<PertumbuhanWithDetail>> {
        return pertumbuhanDao.getPertumbuhanWithDetailFlow(idAnak)
    }

    suspend fun getStandarWHO(idJenis: Int, jenisKelamin: String): List<StandarPertumbuhanEntity> {
        val result = pertumbuhanDao.getStandarPertumbuhan(idJenis, jenisKelamin)
        Log.d("RepositoryDebug", "standarWHO result: $result")
        return result
    }

    suspend fun getJenisPertumbuhan(): List<JenisPertumbuhanEntity> {
        return pertumbuhanDao.getAllJenisPertumbuhan()
    }

    suspend fun syncStandarPertumbuhan() {
        try {
            val response = apiService.getStandarPertumbuhan()
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("SyncStandar", "Response berhasil: ${response.code()}")
                Log.d("SyncStandar", "Total data diterima: ${body?.data?.size ?: 0}")

                body?.data?.let { dtoList ->
                    val entities = dtoList.mapIndexed { index, dto ->
                        try {
                            dto.toEntity()
                        } catch (e: Exception) {
                            Log.e("SyncStandar", "Gagal convert data ke-${index} : ${dto}", e)
                            null
                        }
                    }.filterNotNull()

                    Log.d("SyncStandar", "Total data berhasil dikonversi: ${entities.size}")
                    standarDao.insertAll(entities)
                    Log.d("SyncStandar", "Data berhasil disimpan ke Room")
                }
            } else {
                Log.e("SyncStandar", "Response gagal: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("SyncStandar", "Exception saat sync", e)
        }
    }

    suspend fun deletePertumbuhan(idLocal: Int) {
        // Ambil data pertumbuhan berdasarkan id lokal
        val data = pertumbuhanDao.getPertumbuhanId(idLocal)
        if (data == null) {
            Log.w("DELETE_PERTUMBUHAN", "Data dengan idLocal=$idLocal tidak ditemukan di lokal")
            return
        }

        // Cek apakah data sudah tersinkronisasi dengan API (idApiPertumbuhan tidak null)
        data.idApiPertumbuhan?.let { idApi ->
            try {
                Log.d("DELETE_PERTUMBUHAN", "Menghapus data dari API dengan idApi=$idApi")
                val response = apiService.deletePertumbuhan(idApi)
                if (response.isSuccessful) {
                    Log.d("DELETE_PERTUMBUHAN", "Berhasil menghapus data di API dengan idApi=$idApi")
                } else {
                    Log.w("DELETE_PERTUMBUHAN", "Gagal menghapus di API dengan idApi=$idApi. Code: ${response.code()}, Message: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("DELETE_PERTUMBUHAN", "Error saat menghapus data di API dengan idApi=$idApi: ${e.message}", e)
            }
        } ?: run {
            Log.d("DELETE_PERTUMBUHAN", "Data belum tersinkronisasi dengan API (idApiPertumbuhan=null), hanya hapus lokal")
        }

        try {
            pertumbuhanDao.deleteDetailsByPertumbuhanId(idLocal)
            pertumbuhanDao.deletePertumbuhanById(idLocal)
            Log.d("DELETE_PERTUMBUHAN", "Berhasil menghapus data lokal dengan idLocal=$idLocal")
        } catch (e: Exception) {
            Log.e("DELETE_PERTUMBUHAN", "Gagal menghapus data lokal dengan idLocal=$idLocal: ${e.message}", e)
        }
    }
}