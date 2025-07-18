package com.example.grow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DetailPertumbuhanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: DetailPertumbuhanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDetails(details: List<DetailPertumbuhanEntity>)

    @Query("SELECT * FROM detail_pertumbuhan WHERE idPertumbuhan = :idPertumbuhan")
    suspend fun getDetailByPertumbuhanId(idPertumbuhan: Int): List<DetailPertumbuhanEntity>
}
