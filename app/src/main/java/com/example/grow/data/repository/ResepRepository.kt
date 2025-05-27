package com.example.grow.data.repository

import com.example.grow.data.ResepDao
import com.example.grow.data.remote.ResepApiService
import com.example.grow.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResepRepository @Inject constructor(
    private val dao: ResepDao,
    private val apiService: ResepApiService
) {
    suspend fun getAllResep(): List<ResepEntity> {
        val apiResep = apiService.getAllResep()
        if (apiResep.isSuccessful) {
            apiResep.body()?.let { resepList ->
                // Sinkronisasi data lokal
                resepList.forEach { resep ->
                    dao.insertResep(resep)

                    // Sinkronisasi bahan
                    val bahanResponse = apiService.getAllBahan()
                    if (bahanResponse.isSuccessful) {
                        bahanResponse.body()?.let { bahanList ->
                            // Insert setiap bahan satu per satu
                            bahanList.forEach { bahan ->
                                dao.insertBahan(bahan)
                            }
                        }
                    }

                    // Sinkronisasi hubungan makanan-bahan untuk resep ini
                    val bahanForResepResponse = apiService.getBahanByResep(resep.idMakanan)
                    if (bahanForResepResponse.isSuccessful) {
                        bahanForResepResponse.body()?.let { bahanForResep ->
                            val resepBahanList = bahanForResep.map { bahan ->
                                ResepBahanEntity(resep.idMakanan, bahan.idBahan, null, null)
                            }
                            dao.insertResepBahan(resepBahanList)
                        }
                    }

                    // Sinkronisasi langkah
                    val langkahResponse = apiService.getLangkahByResep(resep.idMakanan)
                    if (langkahResponse.isSuccessful) {
                        langkahResponse.body()?.let { langkahList ->
                            // Insert setiap langkah satu per satu
                            langkahList.forEach { langkah ->
                                dao.insertLangkah(langkah)
                            }
                        }
                    }

                    // Sinkronisasi nutrisi
                    val nutrisiResponse = apiService.getNutrisiByResep(resep.idMakanan)
                    if (nutrisiResponse.isSuccessful) {
                        nutrisiResponse.body()?.let { nutrisiList ->
                            // Insert setiap nutrisi satu per satu
                            nutrisiList.forEach { nutrisi ->
                                dao.insertNutrisi(nutrisi)
                            }
                        }
                    }
                }
            }
        }
        return dao.getAllResep()
    }

    suspend fun getBahanByResep(idMakanan: Int): List<BahanEntity> {
        return dao.getBahanByResep(idMakanan)
    }

    suspend fun getLangkahByResep(idMakanan: Int): List<LangkahPembuatanEntity> {
        return dao.getLangkahByResep(idMakanan)
    }

    suspend fun getNutrisiByResep(idMakanan: Int): List<NutrisiEntity> {
        return dao.getNutrisiByResep(idMakanan)
    }
}