package com.example.grow.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.Anak
import com.example.grow.data.AnakEntity
import com.example.grow.data.repository.AnakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AnakViewModel @Inject constructor(
    private val anakRepository: AnakRepository
) : ViewModel() {

    fun fetchAllAnakFromApi() {
        viewModelScope.launch {
            try {
                // Mengambil data anak dari API dan menyimpannya ke database lokal
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
                // Mengambil data anak dari Room sebagai Flow
                anakRepository.getAnakById(id).collect { anak ->
                    onSuccess(anak)  // Menyampaikan data anak ke onSuccess
                }
            } catch (e: Exception) {
                onError(e.message ?: "Terjadi kesalahan")  // Menyampaikan error ke onError
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
