package com.example.grow.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.CatatanKehamilan
import com.example.grow.data.model.KehamilanWithCatatan
import com.example.grow.data.remote.CatatanKehamilanApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatatanKehamilanViewModel @Inject constructor(
    private val api: CatatanKehamilanApiService
) : ViewModel() {

    var kehamilan by mutableStateOf<KehamilanWithCatatan?>(null)
        private set

    var catatanList by mutableStateOf<List<CatatanKehamilan>>(emptyList())
        private set

    fun fetchKehamilan(userId: Int) {
        viewModelScope.launch {
            try {
                Log.d("CatatanKehamilan", "Memanggil fetchKehamilan untuk userId: $userId")
                val response = api.getKehamilanByUserId(userId)
                kehamilan = response.data
                catatanList = response.data.catatan_kehamilan ?: emptyList()
                Log.d("CatatanKehamilan", "Berhasil mengambil data kehamilan dan catatan.")
            } catch (e: Exception) {
                Log.e("CatatanKehamilan", "Gagal mengambil data kehamilan: ${e.message}", e)
            }
        }
    }

    fun simpanCatatan(userId: Int, catatan: CatatanKehamilan) {
        viewModelScope.launch {
            try {
                api.postCatatanKehamilan(catatan)
                fetchKehamilan(userId)
            } catch (e: Exception) {
                Log.e("CatatanKehamilan", "Gagal menyimpan catatan: ${e.message}", e)
            }
        }
    }

    suspend fun updateCatatan(id: Int, berat: Float, tanggal: String) {
        try {
            // panggil API update
            api.updateCatatan(id, berat, tanggal)
            fetchCatatanByKehamilanId(currentKehamilanId) // refresh data
        } catch (e: Exception) {
            Log.e("CatatanVM", "Update gagal: ${e.message}")
        }
    }

    suspend fun hapusCatatan(id: Int) {
        try {
            api.deleteCatatan(id)
            fetchCatatanByKehamilanId(currentKehamilanId)
        } catch (e: Exception) {
            Log.e("CatatanVM", "Hapus gagal: ${e.message}")
        }
    }
}




