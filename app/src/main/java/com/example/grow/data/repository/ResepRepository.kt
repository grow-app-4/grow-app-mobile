package com.example.grow.data.repository

import android.util.Log
import com.example.grow.data.model.*
import com.example.grow.data.remote.ResepApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import com.example.grow.R

@Singleton
class ResepRepository @Inject constructor(
    private val apiService: ResepApiService
) {
    private val TAG = "ResepRepository"

    suspend fun getAllResep(): List<Resep> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResep = apiService.getAllResep()
                Log.d(TAG, "getAllResep - Raw API Response: ${apiResep.body()}")
                if (apiResep.isSuccessful && apiResep.body()?.success == true) {
                    val data = apiResep.body()?.data
                    Log.d(TAG, "getAllResep - Extracted Data: $data")
                    data ?: emptyList()
                } else {
                    throw Exception("API gagal: ${apiResep.code()} - ${apiResep.message()}")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                throw Exception("Gagal terhubung ke server: ${e.message}")
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error: ${e.message}")
                throw Exception("Error server: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Unknown error: ${e.message}")
                throw e
            }
        }
    }

    suspend fun getBahanByResep(idResep: String): List<BahanItem> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.getBahanByResep(idResep)
                Log.d(TAG, "getBahanByResep - Raw API Response for $idResep: ${apiResponse.body()}")
                if (apiResponse.isSuccessful && apiResponse.body()?.success == true) {
                    val data = apiResponse.body()?.data
                    Log.d(TAG, "getBahanByResep - Extracted Data for $idResep: $data")
                    (data as? List<Map<String, Any>>)?.map { mapToBahanItem(it, idResep) } ?: emptyList()
                } else {
                    throw Exception("Gagal mengambil bahan: ${apiResponse.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting bahan for resep $idResep: ${e.message}")
                throw e
            }
        }
    }

    suspend fun getLangkahByResep(idResep: String): List<LangkahItem> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.getLangkahByResep(idResep)
                Log.d(TAG, "getLangkahByResep - Raw API Response for $idResep: ${apiResponse.body()}")
                if (apiResponse.isSuccessful && apiResponse.body()?.success == true) {
                    val data = apiResponse.body()?.data
                    Log.d(TAG, "getLangkahByResep - Extracted Data for $idResep: $data")
                    (data as? List<Map<String, Any>>)?.map { mapToLangkahItem(it, idResep) } ?: emptyList()
                } else {
                    throw Exception("Gagal mengambil langkah: ${apiResponse.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting langkah for resep $idResep: ${e.message}")
                throw e
            }
        }
    }

    suspend fun getNutrisiByResep(idResep: String): List<NutrisiItem> {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.getNutrisiByResep(idResep)
                Log.d(TAG, "getNutrisiByResep - Raw API Response for $idResep: ${apiResponse.body()}")
                if (apiResponse.isSuccessful && apiResponse.body()?.success == true) {
                    val data = apiResponse.body()?.data
                    Log.d(TAG, "getNutrisiByResep - Extracted Data for $idResep: $data")
                    (data as? List<Map<String, Any>>)?.map { mapToNutrisiItem(it, idResep) } ?: emptyList()
                } else {
                    throw Exception("Gagal mengambil nutrisi: ${apiResponse.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting nutrisi for resep $idResep: ${e.message}")
                throw e
            }
        }
    }

    suspend fun getTotalHargaResep(idResep: String): Double {
        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = apiService.getTotalHarga(idResep)
                Log.d(TAG, "getTotalHargaResep - Raw API Response for $idResep: ${apiResponse.body()}")
                if (apiResponse.isSuccessful && apiResponse.body()?.success == true) {
                    val data = apiResponse.body()?.data
                    Log.d(TAG, "getTotalHargaResep - Extracted Data for $idResep: $data")
                    data?.totalHarga ?: 0.0.also {
                        Log.d(TAG, "getTotalHargaResep - Parsed Total Harga for $idResep: $it")
                    }
                } else {
                    Log.w(TAG, "Failed to fetch total harga for $idResep: ${apiResponse.message()}")
                    0.0
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting total harga for resep $idResep: ${e.message}")
                0.0
            }
        }
    }

    suspend fun getResepDetail(resepId: String): Resep {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDetailResep(resepId)
                Log.d(TAG, "getResepDetail - Raw API Response for $resepId: ${response.body()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.success == true && responseBody.data != null) {
                        val resepData = responseBody.data as Map<String, Any>
                        Log.d(TAG, "getResepDetail - Extracted Data for $resepId: $resepData")
                        return@withContext mapToResepDetail(resepData)
                    } else {
                        throw Exception(responseBody?.message ?: "Respons tidak valid")
                    }
                } else {
                    throw Exception("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("ResepRepository", "Error getting detail for resep $resepId: ${e.message}")
                throw Exception("Gagal mengambil detail resep: ${e.message}")
            }
        }
    }

    private fun mapToResepDetail(data: Map<String, Any>): Resep {
        Log.d(TAG, "mapToResepDetail - Input Data: $data")
        val bahan = (data["bahan"] as? List<Map<String, Any>>)?.map {
            BahanItem(
                idResep = data["id_resep"].toString(),
                nama = it["nama_bahan"] as? String ?: "",
                jumlah = "${it["jumlah_bahan"] ?: 0} ${it["satuan"] ?: "gram"}",
                iconResource = R.drawable.ic_food
            )
        } ?: emptyList()
        Log.d(TAG, "mapToResepDetail - Mapped Bahan: $bahan")

        val langkah = (data["langkah_pembuatan"] as? List<Map<String, Any>>)?.map {
            LangkahItem(
                idResep = data["id_resep"].toString(),
                urutan = (it["nomor_langkah"] as? Number)?.toInt() ?: 0,
                deskripsi = it["deskripsi"] as? String ?: ""
            )
        } ?: emptyList()
        Log.d(TAG, "mapToResepDetail - Mapped Langkah: $langkah")

        val nutrisiData = data["kandungan_nutrisi"] as? List<Map<String, Any>>
        Log.d(TAG, "mapToResepDetail - Nutrisi Data: $nutrisiData")
        val nutrisi = if (nutrisiData.isNullOrEmpty()) {
            val nutrisiInfo = data["nutrisi"] as? Map<String, Any>
            Log.d(TAG, "mapToResepDetail - Nutrisi Info (Fallback): $nutrisiInfo")
            if (nutrisiInfo != null) {
                listOf(
                    NutrisiItem(
                        idResep = data["id_resep"].toString(),
                        nama = "Karbohidrat",
                        nilai = "0",
                        satuan = "gram",
                        iconResource = R.drawable.ic_nutrition
                    )
                )
            } else {
                listOf(
                    NutrisiItem(
                        idResep = data["id_resep"].toString(),
                        nama = "Karbohidrat",
                        nilai = "0",
                        satuan = "gram",
                        iconResource = R.drawable.ic_nutrition
                    )
                )
            }
        } else {
            nutrisiData.map {
                NutrisiItem(
                    idResep = data["id_resep"].toString(),
                    nama = it["nama"] as? String ?: "",
                    nilai = it["jumlah"]?.toString() ?: "0",
                    satuan = it["satuan"] as? String ?: "",
                    iconResource = R.drawable.ic_nutrition
                )
            }
        }
        Log.d(TAG, "mapToResepDetail - Mapped Nutrisi: $nutrisi")

        val totalHarga = when (val harga = data["total_harga"]) {
            is Number -> harga.toDouble()
            is String -> harga.toDoubleOrNull() ?: 0.0
            is Map<*, *> -> (data["total_harga"] as? Number)?.toDouble() ?: 0.0
            else -> 0.0
        }
        Log.d(TAG, "mapToResepDetail - Parsed totalHarga for ${data["nama_resep"]}: $totalHarga")

        val resep = Resep(
            idResep = (data["id_resep"] as? Number)?.toString() ?: "",
            namaResep = data["nama_resep"] as? String ?: "",
            chefName = "Chef GROW",
            rating = (data["rating"] as? Number)?.toFloat(),
            imageUrl = data["foto_resep"] as? String,
            usiaRekomendasi = data["usia_rekomendasi"] as? String ?: "N/A",
            totalHarga = totalHarga,
            waktuPembuatan = (data["waktu_pembuatan"] as? Number)?.toInt(),
            namaKategori = data["nama_kategori"] as? String ?: "N/A",
            nutrisi = nutrisi,
            bahan = bahan,
            langkahPembuatan = langkah,
            deskripsi = data["deskripsi"] as? String,
            isBookmarked = false
        )
        Log.d(TAG, "mapToResepDetail - Final Resep: $resep")
        return resep
    }

    private fun mapToNutrisiItem(data: Map<String, Any>, idResep: String): NutrisiItem {
        Log.d(TAG, "mapToNutrisiItem - Input Data for $idResep: $data")
        val nutrisiItem = NutrisiItem(
            idResep = idResep,
            nama = data["nama"] as? String ?: "",
            nilai = data["jumlah"]?.toString() ?: "0",
            satuan = data["satuan"] as? String ?: "",
            iconResource = R.drawable.ic_star // Ganti dengan ikon yang sesuai
        )
        Log.d(TAG, "mapToNutrisiItem - Mapped NutrisiItem: $nutrisiItem")
        return nutrisiItem
    }

    private fun mapToBahanItem(data: Map<String, Any>, idResep: String): BahanItem {
        Log.d(TAG, "mapToBahanItem - Input Data for $idResep: $data")
        val bahanItem = BahanItem(
            idResep = idResep,
            nama = data["nama_bahan"] as? String ?: "",
            jumlah = "${data["jumlah_bahan"]} ${data["satuan"]}",
            iconResource = R.drawable.ic_star // Ganti dengan ikon yang sesuai
        )
        Log.d(TAG, "mapToBahanItem - Mapped BahanItem: $bahanItem")
        return bahanItem
    }

    private fun mapToLangkahItem(data: Map<String, Any>, idResep: String): LangkahItem {
        Log.d(TAG, "mapToLangkahItem - Input Data for $idResep: $data")
        val langkahItem = LangkahItem(
            idResep = idResep,
            urutan = (data["nomor_langkah"] as? Number)?.toInt() ?: 0,
            deskripsi = data["deskripsi"] as? String ?: ""
        )
        Log.d(TAG, "mapToLangkahItem - Mapped LangkahItem: $langkahItem")
        return langkahItem
    }
}