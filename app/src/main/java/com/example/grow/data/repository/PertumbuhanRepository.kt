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
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
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

    // Fungsi fallback buat data hardcoded
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
        Log.d("DEBUG_PERTUMBUHAN", "Inserted pertumbuhan ID: $pertumbuhanId")

        val detailWithId = details.map {
            val detail = it.copy(idPertumbuhan = pertumbuhanId)
            Log.d("DEBUG_DETAIL", "Prepared Detail: idPertumbuhan=${detail.idPertumbuhan}, idJenis=${detail.idJenis}, nilai=${detail.nilai}")
            detail
        }
        val result = pertumbuhanDao.insertDetailPertumbuhan(detailWithId)
        Log.d("DEBUG_DETAIL", "Insert result IDs: $result")
    }

    suspend fun createPertumbuhanToLocal(
        entity: PertumbuhanEntity,
        request: PertumbuhanRequest,
        localJenis: List<JenisPertumbuhanEntity>
    ) {
        Log.d("LOCAL_INSERT", "Memulai penyimpanan data pertumbuhan ke lokal...")

        // 2. Simpan entitas pertumbuhan dan ambil ID yang di-generate
        Log.d("LOCAL_INSERT", "Menyimpan entitas pertumbuhan: $entity")
        val idPertumbuhan = pertumbuhanDao.insertPertumbuhan(entity).toInt()
        Log.d("LOCAL_INSERT", "ID pertumbuhan yang di-generate: $idPertumbuhan")

        // 3. Mapping data detail pertumbuhan ke entity
        val detailEntities = request.details.map { detail ->
            Log.d("LOCAL_INSERT", "Mapping detail: idJenis=${detail.idJenis}, nilai=${detail.nilai}")
            DetailPertumbuhanEntity(
                idPertumbuhan = idPertumbuhan,
                idJenis = detail.idJenis,
                nilai = detail.nilai
            )
        }

        // 4. Simpan detail pertumbuhan
        Log.d("LOCAL_INSERT", "Menyimpan ${detailEntities.size} detail pertumbuhan")
        pertumbuhanDao.insertDetailPertumbuhan(detailEntities)

        // 5. Proses analisis stunting
        Log.d("LOCAL_INSERT", "Memulai proses analisis stunting untuk ID: $idPertumbuhan")
        prosesAnalisisStunting(idPertumbuhan)

        // 6. Sukses
        Log.d("LOCAL_INSERT", "Data pertumbuhan dan detail berhasil disimpan lokal")
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

    suspend fun createPertumbuhanToApi(request: PertumbuhanRequest): Boolean {
        return try {
            val response = apiService.createPertumbuhan(request)

            if (response.isSuccessful) {
                Log.d("API_POST", "Sukses kirim ke API. Response code: ${response.code()}")
                true
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("API_POST", "Gagal kirim ke API. Code: ${response.code()}, Error: $errorBody")
                false
            }
        } catch (e: CancellationException) {
            Log.e("API_POST", "Coroutine dibatalkan di Repository", e)
            throw e
        } catch (e: Exception) {
            Log.e("API_POST", "Exception saat kirim ke API", e)
            false
        }
    }

    private suspend fun prosesAnalisisStunting(idPertumbuhan: Int) {
        try {
            Log.d("AnalisisStunting", "Mulai analisis untuk idPertumbuhan: $idPertumbuhan")

            val pertumbuhan = pertumbuhanDao.getPertumbuhanId(idPertumbuhan)
            Log.d("AnalisisStunting", "Data pertumbuhan: $pertumbuhan")

            val anak = anakDao.getAnakId(pertumbuhan.idAnak)
            Log.d("AnalisisStunting", "Data anak: $anak")

            val detailList = detailDao.getDetailByPertumbuhanId(idPertumbuhan)
            Log.d("AnalisisStunting", "Detail pertumbuhan: $detailList")

            val detailTinggi = detailList.firstOrNull { it.idJenis == JENIS_TINGGI_BADAN }
            if (detailTinggi == null) {
                Log.w("AnalisisStunting", "Detail tinggi badan tidak ditemukan, proses dihentikan")
                return
            }
            Log.d("AnalisisStunting", "Detail tinggi badan: $detailTinggi")

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

            val usiaBulan = ChronoUnit.MONTHS.between(
                LocalDate.parse(anak.tanggalLahir),
                LocalDate.parse(pertumbuhan.tanggalPencatatan, formatter)
            ).toInt()

            Log.d("AnalisisStunting", "Usia dalam bulan: $usiaBulan")

            val standar = standarDao.getStandarByJenisUsiaKelamin(
                JENIS_TINGGI_BADAN, usiaBulan, anak.jenisKelamin
            )
            Log.d("AnalisisStunting", "Data standar pertumbuhan: $standar")

            val zScore = (detailTinggi.nilai - standar.nilai) / standar.z_score
            Log.d("AnalisisStunting", "Hasil perhitungan zScore: $zScore")

            val status = mapZScoreToStatus(zScore)
            Log.d("AnalisisStunting", "Status hasil analisis: $status")

            pertumbuhanDao.updateStatusStunting(idPertumbuhan, status)
            Log.d("AnalisisStunting", "Berhasil update status stunting ke database")
        } catch (e: Exception) {
            Log.e("AnalisisStunting", "Terjadi error saat analisis", e)
        }
    }

    private fun mapZScoreToStatus(zScore: Float): String {
        return when {
            zScore < -3 -> "Sangat Pendek"
            zScore < -2 -> "Pendek"
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

    // Standar WHO (suspend)
    suspend fun getStandarWHO(idJenis: Int, jenisKelamin: String): List<StandarPertumbuhanEntity> {
        val result = pertumbuhanDao.getStandarPertumbuhan(idJenis, jenisKelamin)
        Log.d("RepositoryDebug", "standarWHO result: $result")
        return result
    }

    // Jenis pertumbuhan (suspend)
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

    suspend fun deletePertumbuhan(idPertumbuhan: Int) {
        pertumbuhanDao.deleteDetailPertumbuhanById(idPertumbuhan)
        pertumbuhanDao.deletePertumbuhanById(idPertumbuhan)
    }
}