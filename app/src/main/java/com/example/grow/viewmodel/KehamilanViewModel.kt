package com.example.grow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.KehamilanRequest
import com.example.grow.data.model.KehamilanResponse
import com.example.grow.data.model.UsiaKehamilanResponse
import com.example.grow.data.remote.KehamilanApiService
import com.example.grow.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KehamilanViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val api: KehamilanApiService
) : ViewModel() {

    private val _kehamilanResult = MutableStateFlow<KehamilanResponse?>(null)
    val kehamilanResult = _kehamilanResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _usiaKehamilan = MutableStateFlow<UsiaKehamilanResponse?>(null)
    val usiaKehamilan: StateFlow<UsiaKehamilanResponse?> = _usiaKehamilan

    private val _namaPengguna = MutableStateFlow<String?>(null)
    val namaPengguna: StateFlow<String?> = _namaPengguna.asStateFlow()

    fun tambahKehamilan(idUser: Int, tanggal: String, berat: Float) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = api.tambahKehamilan(
                    KehamilanRequest(
                        id_user = idUser,
                        tanggal_mulai = tanggal,
                        berat_awal = berat
                    )
                )
                _kehamilanResult.value = response
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUsiaKehamilan(userId: Int) {
        viewModelScope.launch {
            try {
                val response = api.getUsiaKehamilan(userId)
                Log.d("KehamilanViewModel", "Response: $response")
                _usiaKehamilan.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("KehamilanViewModel", "Exception: ${e.message}")
                _usiaKehamilan.value = null
            }
        }
    }
    fun loadUserData(userId: Int) {
        viewModelScope.launch {
            // Ambil data pengguna dari repository
            userRepository.getUserById(userId).collect { user ->
                _namaPengguna.value = user?.name ?: "Nama Pengguna"
            }
        }
    }
}