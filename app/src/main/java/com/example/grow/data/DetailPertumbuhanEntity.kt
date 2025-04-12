package com.example.grow.data

import androidx.room.Entity
import androidx.room.*

@Entity(
    tableName = "detail_pertumbuhan",
    primaryKeys = ["idPertumbuhan", "idJenis"],
    foreignKeys = [
        ForeignKey(
            entity = PertumbuhanEntity::class,
            parentColumns = ["idPertumbuhan"],
            childColumns = ["idPertumbuhan"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JenisPertumbuhanEntity::class,
            parentColumns = ["idJenis"],
            childColumns = ["idJenis"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DetailPertumbuhanEntity(
    val idPertumbuhan: Int,
    val idJenis: Int,
    val nilai: Float
)

data class PertumbuhanWithDetail(
    @Embedded val pertumbuhan: PertumbuhanEntity,
    @Relation(
        parentColumn = "idPertumbuhan",
        entityColumn = "idPertumbuhan",
        entity = DetailPertumbuhanEntity::class
    )
    val details: List<DetailPertumbuhanWithJenis>
)

data class DetailPertumbuhanWithJenis(
    @Embedded val detail: DetailPertumbuhanEntity,
    @Relation(
        parentColumn = "idJenis",
        entityColumn = "idJenis"
    )
    val jenis: JenisPertumbuhanEntity
)

data class DetailWithTanggal(
    val idPertumbuhan: Int,
    val nilai: Float,
    val tanggalPencatatan: String
)

