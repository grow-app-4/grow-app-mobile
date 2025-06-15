package com.example.grow.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.CatatanKehamilan
import com.example.grow.data.model.KehamilanWithCatatan
import com.example.grow.data.model.StatusUpdateRequest
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

    fun hapusCatatan(userId: Int, catatan: CatatanKehamilan) {
        viewModelScope.launch {
            try {
                api.deleteCatatanKehamilan(catatan.id_kehamilan, catatan.tanggal)
                fetchKehamilan(userId)
            } catch (e: Exception) {
                Log.e("CatatanKehamilan", "Gagal menghapus catatan: ${e.message}", e)
            }
        }
    }

    fun updateCatatan(userId: Int, catatan: CatatanKehamilan) {
        viewModelScope.launch {
            try {
                api.updateCatatanKehamilan(
                    catatan.id_kehamilan,
                    catatan.tanggal,
                    catatan
                )
                fetchKehamilan(userId)
            } catch (e: Exception) {
                Log.e("CatatanKehamilan", "Gagal mengupdate catatan: ${e.message}", e)
            }
        }
    }

    fun updateStatusKehamilan(id: Int, status: String) {
        viewModelScope.launch {
            try {
                api.updateStatusKehamilan(id, StatusUpdateRequest(status))
                // Refresh agar state lokal ter-update
                kehamilan = kehamilan?.copy(status = status)
            } catch (e: Exception) {
                Log.e("KehamilanViewModel", "Gagal update status: ${e.message}", e)
            }
        }
    }
}




