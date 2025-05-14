package com.example.grow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grow.data.model.*

@Dao
interface ResepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResep(resep: ResepEntity)

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

    @Query("SELECT * FROM makanan ORDER BY idMakanan DESC")
    suspend fun getAllResep(): List<ResepEntity>

    @Query("SELECT * FROM bahan_baku")
    suspend fun getAllBahan(): List<BahanEntity>

    @Query("SELECT b.* FROM makanan_bahan mb JOIN bahan_baku b ON mb.idBahan = b.idBahan WHERE mb.idMakanan = :idMakanan")
    suspend fun getBahanByResep(idMakanan: Int): List<BahanEntity>

    @Query("SELECT * FROM langkah_pembuatan WHERE idMakanan = :idMakanan ORDER BY urutan ASC")
    suspend fun getLangkahByResep(idMakanan: Int): List<LangkahPembuatanEntity>

    @Query("SELECT n.* FROM makanan_nutrisi mn JOIN nutrisi n ON mn.idNutrisi = n.idNutrisi WHERE mn.idMakanan = :idMakanan")
    suspend fun getNutrisiByResep(idMakanan: Int): List<NutrisiEntity>
}