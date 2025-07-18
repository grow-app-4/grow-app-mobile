package com.example.grow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnakDao {
    @Query("SELECT * FROM anak")
    fun getAllAnak(): Flow<List<AnakEntity>>

    @Query("SELECT * FROM anak WHERE idAnak = :id")
    fun getAnakById(id: Int): Flow<AnakEntity?>

    @Query("SELECT * FROM anak WHERE idAnak = :idAnak")
    suspend fun getAnakId(idAnak: Int): AnakEntity

    @Query("SELECT * FROM anak WHERE idUser = :userId")
    suspend fun getChildrenByUserId(userId: Int): List<AnakEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAnak(anak: List<AnakEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnak(anak: AnakEntity): Long

    @Query("SELECT * FROM anak WHERE idUser = :userId")
    fun getAnakByUserId(userId: Int): Flow<List<AnakEntity>>

    @Update
    suspend fun updateAnak(anak: AnakEntity)

    @Delete
    suspend fun deleteAnak(anak: AnakEntity)
}
