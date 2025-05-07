package com.example.grow.data.repository

import android.util.Log
import com.example.grow.data.AnakDao
import com.example.grow.data.remote.AnakApiService
import com.example.grow.data.AnakEntity
import com.example.grow.data.DetailPertumbuhanDao
import com.example.grow.data.DetailPertumbuhanEntity
import com.example.grow.data.PertumbuhanDao
import com.example.grow.data.PertumbuhanEntity
import com.example.grow.data.model.AnakRequest
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

    // Mengambil data dari API,
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
                        tanggalLahir = anak.tanggalLahir.toString()
                    )
                }
                anakDao.insertAllAnak(entities)
                val anakDataFromRoom = anakDao.getAllAnak().firstOrNull()
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

        try {
            // 1. Siapkan request ke API
            val anakRequest = AnakRequest(
                idUser = anak.idUser,
                namaAnak = anak.namaAnak,
                jenisKelamin = anak.jenisKelamin,
                tanggalLahir = anak.tanggalLahir
            )

            val response = anakApiService.createAnak(anakRequest)

            if (response.isSuccessful) {
                val anakResponse = response.body()?.data
                val idAnakFromApi = anakResponse?.idAnak

                Log.d(TAG, "Anak berhasil dibuat di API. ID dari server: $idAnakFromApi")

                if (idAnakFromApi != null) {
                    val anakWithApiId = anak.copy(idAnak = idAnakFromApi)
                    anakDao.insertAnak(anakWithApiId)

                    insertInitialGrowth(idAnakFromApi, anak.tanggalLahir, beratLahir, tinggiLahir, lingkarKepalaLahir)

                } else {
                    Log.e(TAG, "Gagal mendapatkan ID anak dari response. Simpan ke lokal saja.")
                    saveAnakLocally(anak, beratLahir, tinggiLahir, lingkarKepalaLahir)
                }

            } else {
                val error = response.errorBody()?.string()
                Log.e(TAG, "Gagal membuat anak di API: $error. Simpan ke lokal saja.")
                saveAnakLocally(anak, beratLahir, tinggiLahir, lingkarKepalaLahir)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Terjadi error saat menambahkan anak: ${e.message}. Simpan ke lokal saja.")
            saveAnakLocally(anak, beratLahir, tinggiLahir, lingkarKepalaLahir)
        }
    }

    private suspend fun saveAnakLocally(
        anak: AnakEntity,
        beratLahir: Float,
        tinggiLahir: Float,
        lingkarKepalaLahir: Float
    ) {
        val idAnakLocal = anakDao.insertAnak(anak).toInt()
        insertInitialGrowth(idAnakLocal, anak.tanggalLahir, beratLahir, tinggiLahir, lingkarKepalaLahir)
        Log.d("AddAnakLog", "Anak disimpan ke lokal dengan ID: $idAnakLocal (offline mode)")
    }

    private suspend fun insertInitialGrowth(
        idAnak: Int,
        tanggalLahir: String,
        berat: Float,
        tinggi: Float,
        lingkarKepala: Float
    ) {
        val pertumbuhanEntity = PertumbuhanEntity(
            idPertumbuhan = 0,
            idAnak = idAnak,
            tanggalPencatatan = tanggalLahir,
            statusStunting = ""
        )

        val pertumbuhanId = pertumbuhanDao.insertPertumbuhan(pertumbuhanEntity).toInt()

        val detailList = listOf(
            DetailPertumbuhanEntity(pertumbuhanId, 2, berat),
            DetailPertumbuhanEntity(pertumbuhanId, 1, tinggi),
            DetailPertumbuhanEntity(pertumbuhanId, 3, lingkarKepala)
        )

        detailList.forEach {
            detailPertumbuhanDao.insertDetail(it)
        }

        Log.d("AddAnakLog", "Pertumbuhan awal disimpan untuk anak ID: $idAnak")
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


    suspend fun deleteAnak(anak: AnakEntity) {
        anakDao.deleteAnak(anak)
        anakApiService.deleteAnak(anak.idAnak)
    }
}
