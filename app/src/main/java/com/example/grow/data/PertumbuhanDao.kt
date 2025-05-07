package com.example.grow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PertumbuhanDao {
    @Transaction
    @Query("SELECT * FROM pertumbuhan")
    suspend fun getAllPertumbuhan(): List<PertumbuhanWithDetail>

    @Transaction
    @Query("SELECT * FROM pertumbuhan WHERE idPertumbuhan = :idPertumbuhan")
    suspend fun getPertumbuhanById(idPertumbuhan: Int): PertumbuhanWithDetail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPertumbuhan(pertumbuhan: PertumbuhanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetailPertumbuhan(details: List<DetailPertumbuhanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJenisPertumbuhan(jenisList: List<JenisPertumbuhanEntity>)

    @Transaction
    @Query("SELECT * FROM pertumbuhan WHERE idAnak = :anakId")
    suspend fun getPertumbuhanByAnak(anakId: Int): List<PertumbuhanWithDetail>

    @Query("SELECT * FROM pertumbuhan WHERE idPertumbuhan = :id")
    suspend fun getPertumbuhanId(id: Int): PertumbuhanEntity

    @Query("UPDATE pertumbuhan SET statusStunting = :status WHERE idPertumbuhan = :id")
    suspend fun updateStatusStunting(id: Int, status: String)

    @Query("SELECT statusStunting FROM pertumbuhan WHERE idAnak = :idAnak ORDER BY idPertumbuhan DESC LIMIT 1")
    suspend fun getLatestStatusStunting(idAnak: Int): String?

    @Query("""
    SELECT jp.namaJenis, dp.nilai 
    FROM detail_pertumbuhan dp
    INNER JOIN jenis_pertumbuhan jp ON dp.idJenis = jp.idJenis
    INNER JOIN pertumbuhan p ON dp.idPertumbuhan = p.idPertumbuhan
    WHERE p.idAnak = :idAnak
    AND p.idPertumbuhan = (
        SELECT idPertumbuhan FROM pertumbuhan
        WHERE idAnak = :idAnak
        ORDER BY idPertumbuhan DESC
        LIMIT 1
    )
""")
    suspend fun getLatestPertumbuhanByIdAnak(idAnak: Int): List<GrowthData>

    @Query("""
    SELECT tanggalPencatatan FROM pertumbuhan
    WHERE idAnak = :idAnak
    ORDER BY idPertumbuhan DESC
    LIMIT 1
""")
    suspend fun getTanggalPencatatanTerbaru(idAnak: Int): String?


    @Query("DELETE FROM pertumbuhan")
    suspend fun deleteAllPertumbuhan()

    @Query("DELETE FROM detail_pertumbuhan")
    suspend fun deleteAllDetailPertumbuhan()

    @Query("DELETE FROM jenis_pertumbuhan")
    suspend fun deleteAllJenisPertumbuhan()

    @Query("DELETE FROM detail_pertumbuhan WHERE idPertumbuhan = :idPertumbuhan")
    suspend fun deleteDetailsByPertumbuhanId(idPertumbuhan: Int)

    @Query("""
    SELECT dp.idPertumbuhan, dp.nilai, p.tanggalPencatatan 
    FROM detail_pertumbuhan dp
    INNER JOIN pertumbuhan p ON dp.idPertumbuhan = p.idPertumbuhan
    WHERE p.idAnak = :idAnak AND dp.idJenis = :idJenis
""")
    suspend fun getDetailPertumbuhanWithTanggal(
        idAnak: Int,
        idJenis: Int
    ): List<DetailWithTanggal>

    @Transaction
    @Query("SELECT * FROM pertumbuhan WHERE idAnak = :idAnak ORDER BY tanggalPencatatan ASC")
    fun getPertumbuhanWithDetailFlow(idAnak: Int): Flow<List<PertumbuhanWithDetail>>

    // Ambil standar WHO sesuai jenis dan jenis kelamin anak (biasanya statis, jadi cukup suspend)
    @Query("SELECT * FROM standar_pertumbuhan WHERE id_jenis = :idJenis AND jenis_kelamin = :jenisKelamin ORDER BY usia ASC, z_score ASC")
    suspend fun getStandarPertumbuhan(idJenis: Int, jenisKelamin: String): List<StandarPertumbuhanEntity>

    // Ambil semua jenis pertumbuhan untuk dropdown atau label
    @Query("SELECT * FROM jenis_pertumbuhan")
    suspend fun getAllJenisPertumbuhan(): List<JenisPertumbuhanEntity>

    @Update
    suspend fun updatePertumbuhan(pertumbuhan: PertumbuhanEntity)

    @Update
    suspend fun updateDetailPertumbuhan(details: List<DetailPertumbuhanEntity>)

    @Query("DELETE FROM pertumbuhan WHERE idPertumbuhan = :idPertumbuhan")
    suspend fun deletePertumbuhanById(idPertumbuhan: Int)

    @Query("DELETE FROM detail_pertumbuhan WHERE idPertumbuhan = :idPertumbuhan")
    suspend fun deleteDetailPertumbuhanById(idPertumbuhan: Int)


}

