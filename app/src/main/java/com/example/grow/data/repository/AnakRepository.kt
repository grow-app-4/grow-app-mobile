package com.example.grow.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.grow.data.AnakDao
import com.example.grow.data.remote.AnakApiService
import com.example.grow.data.AnakEntity
import com.example.grow.data.DetailPertumbuhanDao
import com.example.grow.data.DetailPertumbuhanEntity
import com.example.grow.data.PertumbuhanDao
import com.example.grow.data.PertumbuhanEntity
import com.example.grow.data.model.AnakRequest
import com.example.grow.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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
    suspend fun fetchAllAnakFromApi(context: Context) {
        val response = anakApiService.getAllAnak()
        if (response.isSuccessful && response.body() != null) {
            response.body()?.data?.let { anakList ->
                val userId = SessionManager.getUserId(context)

                val filteredAnakList = anakList.filter { it.idUser == userId }

                val entities = filteredAnakList.map { anak ->
                    AnakEntity(
                        idAnak = anak.idAnak,
                        idUser = anak.idUser,
                        namaAnak = anak.namaAnak,
                        jenisKelamin = anak.jenisKelamin,
                        tanggalLahir = anak.tanggalLahir.toString(),
                        profileImageUri = anak.profileImageUri
                    )
                }

                anakDao.insertAllAnak(entities)

                val anakDataFromRoom = anakDao.getAllAnak().firstOrNull()
                Log.d("AnakRepository", "Data anak dari Room setelah disimpan: $anakDataFromRoom")
            }
        }
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

//    suspend fun updateAnak2(anak: AnakEntity, profileImageUri: Uri?, context: Context) {
//        withContext(Dispatchers.IO) {
//            try {
//                // Convert date to "yyyy-MM-dd" for API
//                val formattedDate = formatTanggalLahir(anak.tanggalLahir)
//
//                // Validate input data
//                require(anak.idAnak > 0) { "ID anak tidak valid: ${anak.idAnak}" }
//                require(anak.idUser > 0) { "ID user tidak valid: ${anak.idUser}" }
//                require(anak.namaAnak.isNotBlank()) { "Nama anak tidak boleh kosong" }
//                require(anak.jenisKelamin in listOf("L", "P")) { "Jenis kelamin tidak valid: ${anak.jenisKelamin}" }
//                require(formattedDate.isNotBlank()) { "Tanggal lahir tidak valid: $formattedDate" }
//
//                // Handle profile photo locally
//                var localProfileImagePath: String? = null
//                if (profileImageUri != null) {
//                    val file = File(context.filesDir, "profile_image_${anak.idAnak}_${System.currentTimeMillis()}.jpg")
//                    try {
//                        context.contentResolver.openInputStream(profileImageUri)?.use { input ->
//                            file.outputStream().use { output -> input.copyTo(output) }
//                        } ?: throw IllegalStateException("Gagal membaca file foto profil")
//                        if (!file.exists() || file.length() == 0L) {
//                            throw IllegalStateException("File foto profil tidak valid atau kosong")
//                        }
//                        localProfileImagePath = file.absolutePath
//                        Log.d("AnakRepository", "Profile photo saved locally: ${file.name}, size=${file.length()} bytes")
//                    } catch (e: Exception) {
//                        Log.e("AnakRepository", "Gagal memproses file foto: ${e.message}", e)
//                        throw IllegalStateException("Gagal memproses file foto: ${e.message}")
//                    }
//                }
//
//                // Update in local database
//                anakDao.updateAnak(
//                    anak.copy(
//                        tanggalLahir = formattedDate,
//                        profileImageUri = localProfileImagePath // Simpan path lokal
//                    )
//                )
//
//                // Prepare API request (JSON)
//                val anakRequest = AnakRequest(
//                    idUser = anak.idUser,
//                    namaAnak = anak.namaAnak.trim(),
//                    jenisKelamin = anak.jenisKelamin,
//                    tanggalLahir = formattedDate
//                    // Tidak mengirim profile_photo ke API
//                )
//
//                // Log request for debugging
//                Log.d("AnakRepository", "Mengirim request: id_anak=${anak.idAnak}, id_user=${anak.idUser}, nama_anak=${anak.namaAnak}, jenis_kelamin=${anak.jenisKelamin}, tanggal_lahir=$formattedDate")
//
//                // Call API
//                val response = anakApiService.updateAnak2(
//                    id = anak.idAnak,
//                    anakRequest = anakRequest
//                )
//
//                if (response.isSuccessful) {
//                    response.body()?.data?.let { anakResponse ->
//                        // Update local database with server data
//                        anakDao.updateAnak(
//                            anak.copy(
//                                tanggalLahir = anakResponse.tanggalLahir,
//                                profileImageUri = localProfileImagePath // Pertahankan path lokal
//                            )
//                        )
//                        Log.d("AnakRepository", "Successfully updated anak: ${anakResponse.namaAnak}")
//                    } ?: throw Exception("Tidak ada data yang dikembalikan dari server")
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    Log.e("AnakRepository", "Error response: $errorBody")
//                    throw Exception("Gagal memperbarui data anak: $errorBody")
//                }
//            } catch (e: Exception) {
//                Log.e("AnakRepository", "Failed to update anak: ${e.message}", e)
//                throw e
//            }
//        }
//    }
    suspend fun updateAnak2(anak: AnakEntity, profileImageUri: Uri?, context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val formattedDate = formatTanggalLahir(anak.tanggalLahir)

                // Validasi input
                require(anak.idAnak > 0) { "ID anak tidak valid: ${anak.idAnak}" }
                require(anak.idUser > 0) { "ID user tidak valid: ${anak.idUser}" }
                require(anak.namaAnak.isNotBlank()) { "Nama anak tidak boleh kosong" }
                require(anak.jenisKelamin in listOf("L", "P")) { "Jenis kelamin tidak valid: ${anak.jenisKelamin}" }

                // Konversi ke RequestBody
                val mediaTypeText = "text/plain".toMediaType()
                val methodBody = "PUT".toRequestBody(mediaTypeText)
                val idUserBody = anak.idUser.toString().toRequestBody(mediaTypeText)
                val namaAnakBody = anak.namaAnak.trim().toRequestBody(mediaTypeText)
                val jenisKelaminBody = anak.jenisKelamin.toRequestBody(mediaTypeText)
                val tanggalLahirBody = formattedDate.toRequestBody(mediaTypeText)

                // Siapkan MultipartBody.Part jika ada foto
                var localProfileImagePath: String? = null
                val profilePhotoPart: MultipartBody.Part? = profileImageUri?.let { uri ->
                    val file = File(context.filesDir, "profile_image_${anak.idAnak}_${System.currentTimeMillis()}.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    } ?: throw IllegalStateException("Gagal membaca file foto profil")

                    if (!file.exists() || file.length() == 0L) {
                        throw IllegalStateException("File foto profil tidak valid atau kosong")
                    }

                    localProfileImagePath = file.absolutePath
                    val requestFile = file.asRequestBody("image/jpeg".toMediaType())
                    MultipartBody.Part.createFormData("profile_photo", file.name, requestFile)
                }

                // Simpan update lokal terlebih dahulu
                anakDao.updateAnak(
                    anak.copy(
                        tanggalLahir = formattedDate,
                        profileImageUri = localProfileImagePath
                    )
                )

                // Panggil API
                val response = anakApiService.updateAnak3(
                    id = anak.idAnak,
                    method = methodBody,
                    idUser = idUserBody,
                    namaAnak = namaAnakBody,
                    jenisKelamin = jenisKelaminBody,
                    tanggalLahir = tanggalLahirBody,
                    profilePhoto = profilePhotoPart
                )

                if (response.isSuccessful) {
                    response.body()?.data?.let { anakResponse ->
                        anakDao.updateAnak(
                            anak.copy(
                                tanggalLahir = anakResponse.tanggalLahir,
                                profileImageUri = localProfileImagePath
                            )
                        )
                        Log.d("AnakRepository", "Berhasil update anak: ${anakResponse.namaAnak}")
                    } ?: throw Exception("Tidak ada data yang dikembalikan dari server")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AnakRepository", "Error response: $errorBody")
                    throw Exception("Gagal memperbarui data anak: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("AnakRepository", "Failed to update anak: ${e.message}", e)
                throw e
            }
        }
    }

    private fun formatTanggalLahir(tanggalLahir: String): String {
        // Cek apakah sudah dalam format yyyy-MM-dd
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(tanggalLahir)
            return tanggalLahir // Kembalikan as-is jika valid
        } catch (e: Exception) {
            Log.d("AnakRepository", "Not in yyyy-MM-dd format: $tanggalLahir")
        }

        // Coba parse dari format dd/MM/yyyy
        try {
            val inputFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate = inputFormatter.parse(tanggalLahir)
            if (parsedDate != null) {
                val outputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                return outputFormatter.format(parsedDate)
            }
        } catch (e: Exception) {
            Log.d("AnakRepository", "Failed to parse date with format dd/MM/yyyy: $tanggalLahir")
        }

        throw IllegalArgumentException("Format tanggal tidak valid: $tanggalLahir")
    }

    suspend fun deleteAnak(anak: AnakEntity) {
        anakDao.deleteAnak(anak)
        anakApiService.deleteAnak(anak.idAnak)
    }
}
