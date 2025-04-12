package com.example.grow.viewmodel

import android.util.Log
import androidx.core.i18n.DateTimeFormatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.*
import com.example.grow.data.repository.AnakRepository
import com.example.grow.data.repository.PertumbuhanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class GrafikViewModel @Inject constructor(
    private val repository: PertumbuhanRepository,
    private val anakRepository: AnakRepository
) : ViewModel() {

    // Data grafik pertumbuhan anak: usia (bulan) vs nilai
    private val _grafikAnak = MutableStateFlow<List<Pair<Int, Float>>>(emptyList())
    val grafikAnak: StateFlow<List<Pair<Int, Float>>> = _grafikAnak

    // Data WHO per z-score: Map<Z-score, List<Pair<Usia, Nilai>>>
    private val _grafikWHO = MutableStateFlow<Map<Float, List<Pair<Int, Float>>>>(emptyMap())
    val grafikWHO: StateFlow<Map<Float, List<Pair<Int, Float>>>> = _grafikWHO

    fun loadGrafik(
        anak: AnakEntity,
        idJenis: Int,
    ) {
        Log.d("GrafikDebug", ">> loadGrafik DIPANGGIL dengan idAnak=${anak.idAnak}, idJenis=$idJenis")

        // Jalankan pertumbuhan anak (flow)
        viewModelScope.launch {
            try {
                repository.getPertumbuhanAnak(anak.idAnak)
                    .map { pertumbuhanList ->
                        Log.d("GrafikDebug", ">> Data pertumbuhan anak: $pertumbuhanList")

                        pertumbuhanList.flatMap { p ->
                            p.details
                                .filter { it.jenis.idJenis == idJenis }
                                .mapNotNull { detail ->
                                    val usiaBulan = hitungUsiaDalamBulan(anak.tanggalLahir, p.pertumbuhan.tanggalPencatatan)
                                    usiaBulan?.let { it to detail.detail.nilai }
                                }
                        }.sortedBy { it.first }
                    }
                    .collect { hasil ->
                        Log.d("GrafikDebug", ">> Grafik anak hasil: $hasil")
                        _grafikAnak.value = hasil
                    }
            } catch (e: Exception) {
                Log.e("GrafikDebug", "Error saat load data pertumbuhan anak: ${e.message}", e)
            }
        }

        // Jalankan pengambilan WHO (suspend)
        viewModelScope.launch {
            try {
                val standarWHO = repository.getStandarWHO(idJenis, anak.jenisKelamin)
                Log.d("GrafikDebug", "WHO count = ${standarWHO.size}, isi: $standarWHO")

                val grouped = standarWHO.groupBy { it.z_score }
                    .mapValues { entry ->
                        entry.value.sortedBy { it.usia }
                            .map { it.usia to it.nilai }
                    }

                Log.d("GrafikDebug", "Grouped WHO: $grouped")
                _grafikWHO.value = grouped
            } catch (e: Exception) {
                Log.e("GrafikDebug", "Error saat load WHO: ${e.message}", e)
            }
        }
    }

   fun hitungUsiaDalamBulan(tanggalLahir: String, tanggalPencatatan: String): Int? {
        val formatters = listOf(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")
        )

        fun parseTanggal(tanggal: String): LocalDate? {
            for (formatter in formatters) {
                try {
                    return LocalDate.parse(tanggal, formatter)
                } catch (_: Exception) {}
            }
            return null
        }

        val lahir = parseTanggal(tanggalLahir)
        val catat = parseTanggal(tanggalPencatatan)

        if (lahir != null && catat != null) {
            val tahun = ChronoUnit.YEARS.between(lahir, catat)
            val bulan = ChronoUnit.MONTHS.between(lahir.plusYears(tahun), catat)
            return (tahun * 12 + bulan).toInt()
        }

        return null
    }

    fun getAnakById(id: Int): Flow<AnakEntity?> {
        return anakRepository.getAnakById(id)
    }

    fun syncStandarPertumbuhan() {
        viewModelScope.launch {
            repository.syncStandarPertumbuhan()
        }
    }
}
