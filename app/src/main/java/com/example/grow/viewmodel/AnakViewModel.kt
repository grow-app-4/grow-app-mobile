package com.example.grow.ui.viewmodel

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

    fun fetchAllAnakFromApi() {
        viewModelScope.launch {
            try {
                anakRepository.fetchAllAnakFromApi()
            } catch (e: Exception) {
                Log.e("AnakViewModel", "Error fetching anak data: ${e.message}")
            }
        }
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

    // Fungsi untuk mengupdate anak
    fun updateAnak(anak: AnakEntity) {
        viewModelScope.launch {
            try {
                anakRepository.updateAnak(anak)
            } catch (e: Exception) {
                Log.e("AnakViewModel", "Error updating anak: ${e.message}")
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
