package com.example.grow.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.AnakEntity
import com.example.grow.data.repository.AnakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnakViewModel @Inject constructor(
    private val anakRepository: AnakRepository
) : ViewModel() {

    private var isSynced = false

    fun fetchAllAnakFromApi(context: Context) {
        if (isSynced) {
            Log.d("AnakViewModel", "Anak data sudah disync, skip fetch")
            return
        }

        viewModelScope.launch {
            try {
                anakRepository.fetchAllAnakFromApi(context)
                isSynced = true
                Log.d("AnakViewModel", "Anak data sync sukses")
            } catch (e: Exception) {
                Log.e("AnakViewModel", "Error fetching anak data: ${e.message}")
            }
        }
    }

    fun resetSync() {
        isSynced = false
    }

    // Fungsi untuk mengambil anak dari Room berdasarkan ID
    fun getAnakById(id: Int, onSuccess: (AnakEntity?) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                anakRepository.getAnakById(id).collect { anak ->
                    onSuccess(anak)
                }
            } catch (e: Exception) {
                onError(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    // Fungsi untuk menambahkan anak baru ke Room dan API
    fun addAnak(anak: AnakEntity) {
        viewModelScope.launch {
            try {
                anakRepository.addAnak(anak)
            } catch (e: Exception) {
                Log.e("AnakViewModel", "Error adding anak: ${e.message}")
            }
        }
    }

    // Fungsi untuk menghapus anak
    fun deleteAnak(anak: AnakEntity) {
        viewModelScope.launch {
            try {
                anakRepository.deleteAnak(anak)
            } catch (e: Exception) {
                Log.e("AnakViewModel", "Error deleting anak: ${e.message}")
            }
        }
    }
}
