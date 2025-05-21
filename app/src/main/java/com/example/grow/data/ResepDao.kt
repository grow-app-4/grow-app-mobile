package com.example.grow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grow.data.model.*

@Dao
interface ResepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResep(resep: List<ResepEntity>) // Diperbarui untuk List<ResepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBahan(bahan: BahanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBahan(bahan: List<BahanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResepBahan(resepBahan: List<ResepBahanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLangkah(langkah: LangkahPembuatanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLangkah(langkah: List<LangkahPembuatanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrisi(nutrisi: NutrisiEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrisi(nutrisi: List<NutrisiEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResepNutrisi(resepNutrisi: ResepNutrisiEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResepNutrisi(resepNutrisi: List<ResepNutrisiEntity>)

    @Query("SELECT * FROM resep ORDER BY idResep DESC")
    suspend fun getAllResep(): List<ResepEntity>

    @Query("SELECT * FROM bahan_baku")
    suspend fun getAllBahan(): List<BahanEntity>

    @Query("SELECT b.* FROM resep_bahan rb JOIN bahan_baku b ON rb.idBahan = b.idBahan WHERE rb.idResep = :idResep")
    suspend fun getBahanByResep(idResep: Int): List<BahanEntity>

    @Query("SELECT * FROM langkah_pembuatan WHERE resepId = :idResep ORDER BY nomorLangkah ASC")
    suspend fun getLangkahByResep(idResep: Int): List<LangkahPembuatanEntity>

    @Query("SELECT rb.jumlahBahan, b.hargaBahan FROM resep_bahan rb JOIN bahan_baku b ON rb.idBahan = b.idBahan WHERE rb.idResep = :idResep")
    suspend fun getBahanDanHargaByResep(idResep: Int): List<BahanHargaResult>

    @Query("SELECT n.* FROM kandungan_nutrisi kn JOIN nutrisi n ON kn.idNutrisi = n.idNutrisi WHERE kn.idResep = :idResep")
    suspend fun getNutrisiByResep(idResep: Int): List<NutrisiEntity>

    @Query("SELECT * FROM bahan_baku WHERE idBahan = :idBahan LIMIT 1")
    suspend fun getBahanById(idBahan: Int): BahanEntity?

    @Query("SELECT * FROM resep_bahan WHERE idResep = :idResep")
    suspend fun getResepBahanByResepId(idResep: Int): List<ResepBahanEntity>

    @Query("SELECT * FROM kandungan_nutrisi WHERE idResep = :idResep")
    suspend fun getResepNutrisiByResepId(idResep: Int): List<ResepNutrisiEntity>

    @Query("SELECT * FROM nutrisi")
    suspend fun getAllNutrisi(): List<NutrisiEntity>
}

data class BahanHargaResult(
    val jumlahBahan: Float,
    val hargaBahan: Float
)