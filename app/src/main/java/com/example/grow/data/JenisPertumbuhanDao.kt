package com.example.grow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JenisPertumbuhanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJenis(jenis: JenisPertumbuhanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllJenis(jenisList: List<JenisPertumbuhanEntity>)

    @Query("SELECT * FROM jenis_pertumbuhan")
    fun getAllJenis(): Flow<List<JenisPertumbuhanEntity>>

    @Query("SELECT * FROM jenis_pertumbuhan WHERE idJenis = :id")
    suspend fun getJenisById(id: Int): JenisPertumbuhanEntity?

    @Query("DELETE FROM jenis_pertumbuhan")
    suspend fun deleteAll()
}
