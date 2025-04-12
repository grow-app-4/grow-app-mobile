package com.example.grow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StandarPertumbuhanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<StandarPertumbuhanEntity>)

    @Query("""
        SELECT * FROM standar_pertumbuhan 
        WHERE usia = :usia 
        AND jenis_kelamin = :jenisKelamin 
        AND id_jenis = :idJenis
        ORDER BY z_score
    """)
    suspend fun getStandarByUsiaAndJenisKelamin(
        usia: Int,
        jenisKelamin: String,
        idJenis: Int
    ): List<StandarPertumbuhanEntity>

    @Query("""
    SELECT * FROM standar_pertumbuhan
    WHERE id_jenis = :idJenis AND usia = :usia AND jenis_kelamin = :jenisKelamin
    ORDER BY z_score
""")
    suspend fun getStandarPertumbuhan(
        idJenis: Int,
        usia: Int,
        jenisKelamin: String
    ): List<StandarPertumbuhanEntity>

    @Query("""
        SELECT * FROM standar_pertumbuhan 
        WHERE id_jenis = :idJenis AND usia = :usia AND jenis_kelamin = :jenisKelamin
    """)
    suspend fun getStandarByJenisUsiaKelamin(idJenis: Int, usia: Int, jenisKelamin: String): StandarPertumbuhanEntity

}