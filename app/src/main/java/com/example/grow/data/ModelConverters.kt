package com.example.grow.data

import com.example.grow.data.model.DetailPertumbuhanResponse
import com.example.grow.data.model.JenisPertumbuhanResponse
import com.example.grow.data.model.Pertumbuhan
import com.example.grow.data.model.PertumbuhanRequest
import com.example.grow.data.model.StandarPertumbuhanDto

// Konversi dari Model API ke Entity
fun Pertumbuhan.toEntity(): PertumbuhanEntity {
    return PertumbuhanEntity(
        idPertumbuhan = this.idPertumbuhan,
        idAnak = this.idAnak,
        tanggalPencatatan = this.tanggalPencatatan,
        statusStunting = this.statusStunting
    )
}

fun DetailPertumbuhanResponse.toEntity(): DetailPertumbuhanEntity {
    return DetailPertumbuhanEntity(
        idPertumbuhan = this.idPertumbuhan,
        idJenis = this.idJenis,
        nilai = this.nilai
    )
}

fun JenisPertumbuhanResponse.toEntity(): JenisPertumbuhanEntity {
    return JenisPertumbuhanEntity(
        idJenis = this.idJenis,
        namaJenis = this.namaJenis
    )
}

fun PertumbuhanEntity.toApiModel(): Pertumbuhan {
    return Pertumbuhan(
        idPertumbuhan = this.idPertumbuhan,
        idAnak = this.idAnak,
        tanggalPencatatan = this.tanggalPencatatan,
        statusStunting = this.statusStunting,
        detailPertumbuhan = emptyList() // Diisi manual setelah konversi detail
    )
}

fun PertumbuhanRequest.toEntity(tempId: Int): PertumbuhanEntity {
    return PertumbuhanEntity(
        idPertumbuhan = tempId,
        idAnak = idAnak,
        tanggalPencatatan = tanggalPencatatan,
        statusStunting = statusStunting,
    )
}

fun StandarPertumbuhanDto.toEntity(): StandarPertumbuhanEntity {
    return StandarPertumbuhanEntity(
        id_jenis = this.id_jenis,
        usia = this.usia,
        jenis_kelamin = this.jenis_kelamin,
        z_score = this.z_score,
        nilai = this.nilai
    )
}

