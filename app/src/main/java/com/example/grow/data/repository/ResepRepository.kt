package com.example.grow.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.example.grow.data.model.Resep
import com.example.grow.data.remote.ResepApiService
import com.example.grow.resep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ResepRepository @Inject constructor(
    private val apiService: ResepApiService,
    @Named("BookmarkPrefs") private val sharedPreferences: SharedPreferences
) {
    private val _resepList = MutableStateFlow<List<Resep>>(emptyList())
    val resepList: StateFlow<List<Resep>> = _resepList

    private val _resepDetail = MutableStateFlow<Resep?>(null)
    val resepDetail: StateFlow<Resep?> = _resepDetail

    private val _bookmarkedResepIds = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedResepIds: StateFlow<Set<String>> = _bookmarkedResepIds

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var lastFetchTime: Long = 0
    private val cacheDuration = 5 * 60 * 1000

    init {
        val savedBookmarks = sharedPreferences.getStringSet("bookmarked_resep_ids", emptySet()) ?: emptySet()
        _bookmarkedResepIds.value = savedBookmarks
    }

    suspend fun loadResepList(forceFetch: Boolean = false) {
        if (!forceFetch && _resepList.value.isNotEmpty() && (System.currentTimeMillis() - lastFetchTime) < cacheDuration) {
            Log.d("ResepRepository", "Using cached recipes: ${_resepList.value.size}")
            return
        }

        _loading.value = true
        _error.value = null
        try {
            val response = apiService.getResepList()
            _resepList.value = response.data
            lastFetchTime = System.currentTimeMillis()
            Log.d("ResepRepository", "Loaded ${response.data.size} recipes: ${response.data}")
            response.data.forEach { resep ->
                Log.d("ResepRepository", "Resep: ${resep.namaResep}, Rating: ${resep.rating}, Raw Rating: ${resep.ratingString}")
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Gagal memuat daftar resep"
            Log.e("ResepRepository", "Error: ${e.message}")
        } finally {
            _loading.value = false
        }
    }

    suspend fun loadResepDetail(id: String) {
        _loading.value = true
        _error.value = null
        try {
            val resep = apiService.getResepDetail(id)
            _resepDetail.value = resep
            Log.d("ResepRepository", "Resep detail loaded: $resep")
        } catch (e: Exception) {
            _error.value = e.message ?: "Gagal memuat detail resep"
            Log.e("ResepRepository", "Error: ${e.message}")
        } finally {
            _loading.value = false
        }
    }

    fun toggleBookmark(resep: Resep) {
        val currentBookmarks = _bookmarkedResepIds.value.toMutableSet()
        if (currentBookmarks.contains(resep.idResep)) {
            currentBookmarks.remove(resep.idResep)
        } else {
            currentBookmarks.add(resep.idResep)
        }
        _bookmarkedResepIds.value = currentBookmarks
        sharedPreferences.edit().putStringSet("bookmarked_resep_ids", currentBookmarks).apply()
    }

    fun clearError() {
        _error.value = null
    }
}