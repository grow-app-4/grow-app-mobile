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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class AnakRepository @Inject constructor(
    private val anakApiService: AnakApiService,
    private val anakDao: AnakDao,
    private val pertumbuhanDao: PertumbuhanDao,
    private val detailPertumbuhanDao: DetailPertumbuhanDao
) {

    fun getAllAnak(): Flow<List<AnakEntity>> = anakDao.getAllAnak()

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
                        profileImageUri = anak.profileImageUri?.replace("storage/storage/", "storage/")
                    )
                }
                anakDao.insertAllAnak(entities)
                val anakDataFromRoom = anakDao.getAllAnak().firstOrNull()
                Log.d("AnakRepository", "Data anak dari Room setelah disimpan: $anakDataFromRoom")
            }
        }
    }

    suspend fun addAnakWithInitialGrowth(anak: AnakEntity): Int {
        val TAG = "AddAnakLog"
        try {
            require(anak.namaAnak.isNotBlank()) { "Nama anak tidak boleh kosong" }
            require(anak.jenisKelamin in listOf("L", "P")) { "Jenis kelamin tidak valid" }
            require(anak.tanggalLahir.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { "Format tanggal lahir tidak valid" }

            val anakRequest = AnakRequest(
                idUser = anak.idUser,
                namaAnak = anak.namaAnak,
                jenisKelamin = anak.jenisKelamin,
                tanggalLahir = anak.tanggalLahir
            )
            val response = anakApiService.createAnak(anakRequest)

            if (response.isSuccessful) {
                val idAnakFromApi = response.body()?.data?.idAnak
                if (idAnakFromApi != null) {
                    val anakWithApiId = anak.copy(idAnak = idAnakFromApi)
                    anakDao.insertAnak(anakWithApiId)
                    Log.d(TAG, "Anak disimpan ke API dan lokal dengan ID: $idAnakFromApi")
                    return idAnakFromApi
                }
            }

            val idAnakLocal = anakDao.insertAnak(anak).toInt()
            Log.w(TAG, "Gagal simpan ke API, disimpan lokal dengan ID: $idAnakLocal")
            return idAnakLocal
        } catch (e: Exception) {
            Log.e(TAG, "Error menambahkan anak: ${e.message}", e)
            val idAnakLocal = anakDao.insertAnak(anak).toInt()
            Log.d(TAG, "Disimpan lokal dengan ID: $idAnakLocal (offline)")
            return idAnakLocal
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

        detailList.forEach { detailPertumbuhanDao.insertDetail(it) }
        Log.d("AddAnakLog", "Pertumbuhan awal disimpan untuk anak ID: $idAnak")
    }

    fun getAnakById(id: Int): Flow<AnakEntity?> = anakDao.getAnakById(id)

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

    suspend fun updateAnak2(anak: AnakEntity, profileImageUri: Uri?, context: Context): AnakEntity {
        return withContext(Dispatchers.IO) {
            try {
                val formattedDate = formatTanggalLahir(anak.tanggalLahir)

                require(anak.idAnak > 0) { "ID anak tidak valid: ${anak.idAnak}" }
                require(anak.idUser > 0) { "ID user tidak valid: ${anak.idUser}" }
                require(anak.namaAnak.isNotBlank()) { "Nama anak tidak boleh kosong" }
                require(anak.jenisKelamin in listOf("L", "P")) { "Jenis kelamin tidak valid: ${anak.jenisKelamin}" }

                val mediaTypeText = "text/plain".toMediaType()
                val methodBody = "PUT".toRequestBody(mediaTypeText)
                val idUserBody = anak.idUser.toString().toRequestBody(mediaTypeText)
                val namaAnakBody = anak.namaAnak.trim().toRequestBody(mediaTypeText)
                val jenisKelaminBody = anak.jenisKelamin.toRequestBody(mediaTypeText)
                val tanggalLahirBody = formattedDate.toRequestBody()

                var profileImagePath: String? = anak.profileImageUri
                val profilePhotoPart: MultipartBody.Part? = profileImageUri?.let { uri ->
                    val file = File(context.filesDir, "profile_image_${System.currentTimeMillis()}.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    } ?: throw IllegalStateException("Gagal membaca file foto profil")

                    if (!file.exists() || file.length() == 0L) {
                        throw IllegalStateException("File foto profil tidak valid atau kosong")
                    }

                    profileImagePath = file.absolutePath
                    val requestFile = file.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("profile_photo", file.name, requestFile)
                }

                anakDao.updateAnak(
                    anak.copy(
                        tanggalLahir = formattedDate,
                        profileImageUri = profileImagePath
                    )
                )

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
                        val updatedProfileImageUri = anakResponse.profileImageUri?.replace("/storage//storage/", "/storage/")
                        val updatedAnak = anak.copy(
                            tanggalLahir = anakResponse.tanggalLahir,
                            profileImageUri = updatedProfileImageUri ?: profileImagePath
                        )
                        anakDao.updateAnak(updatedAnak)
                        Log.d("AnakRepository", "Berhasil update anak: ${anakResponse.namaAnak}, profileImageUri: $updatedProfileImageUri")
                        updatedAnak
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
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(tanggalLahir)
            return tanggalLahir
        } catch (e: Exception) {
            Log.d("AnakRepository", "Not in yyyy-MM-dd format: $tanggalLahir")
        }

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