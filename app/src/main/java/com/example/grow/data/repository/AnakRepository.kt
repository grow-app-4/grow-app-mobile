package com.example.grow.data.repository

import android.util.Log
import com.example.grow.data.AnakDao
import com.example.grow.data.model.Anak
import com.example.grow.data.remote.AnakApiService
import com.example.grow.data.AnakEntity
import com.example.grow.data.DetailPertumbuhanDao
import com.example.grow.data.DetailPertumbuhanEntity
import com.example.grow.data.PertumbuhanDao
import com.example.grow.data.PertumbuhanEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AnakRepository @Inject constructor(
    private val anakApiService: AnakApiService,
    private val anakDao: AnakDao,
    private val pertumbuhanDao: PertumbuhanDao,
    private val detailPertumbuhanDao: DetailPertumbuhanDao
) {

    // Mengambil data anak dari database lokal (Room)
    fun getAllAnak(): Flow<List<AnakEntity>> = anakDao.getAllAnak()

    // Mengambil data dari API, lalu menyimpannya ke database lokal
    suspend fun fetchAllAnakFromApi() {
        val response = anakApiService.getAllAnak()
        if (response.isSuccessful && response.body() != null) {
            response.body()?.data?.let { anakList ->
                val entities = anakList.map { anak ->
                    AnakEntity(
                        idAnak = anak.idAnak,
                        idUser = anak.idUser,
                        namaAnak = anak.namaAnak,
                        jenisKelamin = anak.jenisKelamin,
                        tanggalLahir = anak.tanggalLahir.toString() // Ensure this conversion is correct
                    )
                }
                anakDao.insertAllAnak(entities)
                val anakDataFromRoom = anakDao.getAllAnak().firstOrNull() // Mengambil data dari Flow
                Log.d("AnakRepository", "Data anak dari Room setelah disimpan: $anakDataFromRoom")
            }
        }
    }

    fun getChildren(userId: Int): Flow<List<AnakEntity>> {
        return anakDao.getAnakByUserId(userId)
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun addAnakWithInitialGrowth(
        anak: AnakEntity,
        beratLahir: Float,
        tinggiLahir: Float,
        lingkarKepalaLahir: Float
    ) {
        val TAG = "AddAnakLog"

        Log.d(TAG, "Mulai menambahkan anak: ${anak.namaAnak}")

        // Insert anak, dan ambil id hasil insert
        val anakId = anakDao.insertAnak(anak).toInt()
        Log.d(TAG, "Anak berhasil ditambahkan dengan ID: $anakId")

        // Insert ke tabel pertumbuhan
        val pertumbuhanEntity = PertumbuhanEntity(
            idPertumbuhan = 0,
            idAnak = anakId,
            tanggalPencatatan = getCurrentDate(),
            statusStunting = ""
        )

        val pertumbuhanId = pertumbuhanDao.insertPertumbuhan(pertumbuhanEntity).toInt()
        Log.d(TAG, "Pertumbuhan awal berhasil ditambahkan dengan ID: $pertumbuhanId")

        val detailList = listOf(
            DetailPertumbuhanEntity(
                idPertumbuhan = pertumbuhanId,
                idJenis = 2,
                nilai = beratLahir
            ),
            DetailPertumbuhanEntity(
                idPertumbuhan = pertumbuhanId,
                idJenis = 1,
                nilai = tinggiLahir
            ),
            DetailPertumbuhanEntity(
                idPertumbuhan = pertumbuhanId,
                idJenis = 3,
                nilai = lingkarKepalaLahir
            )
        )

        detailList.forEach {
            Log.d(TAG, "Menambahkan detail: idJenis=${it.idJenis}, nilai=${it.nilai}")
            detailPertumbuhanDao.insertDetail(it)
        }

        Log.d(TAG, "Semua data pertumbuhan awal berhasil ditambahkan untuk anak ID: $anakId")
    }

    // Mengambil detail anak dari database lokal
    fun getAnakById(id: Int): Flow<AnakEntity?> {
        return anakDao.getAnakById(id)
    }

    // Menambah anak baru ke database lokal dan mengirimnya ke server
    suspend fun addAnak(anak: AnakEntity) {
        anakDao.insertAnak(anak)
        anakApiService.createAnak(
            hashMapOf(
                "id_user" to anak.idUser,
                "nama_anak" to anak.namaAnak,
                "jenis_kelamin" to anak.jenisKelamin,
                "tanggal_lahir" to anak.tanggalLahir
            )
        )
    }

    // Mengupdate anak di database dan API
    suspend fun updateAnak(anak: AnakEntity) {
        anakDao.updateAnak(anak)
        anakApiService.updateAnak(
            anak.idAnak,
            hashMapOf(
                "id_user" to anak.idUser,
                "nama_anak" to anak.namaAnak,
                "jenis_kelamin" to anak.jenisKelamin,
                "tanggal_lahir" to anak.tanggalLahir
            )
        )
    }

    // Menghapus anak dari database dan API
    suspend fun deleteAnak(anak: AnakEntity) {
        anakDao.deleteAnak(anak)
        anakApiService.deleteAnak(anak.idAnak)
    }
}
