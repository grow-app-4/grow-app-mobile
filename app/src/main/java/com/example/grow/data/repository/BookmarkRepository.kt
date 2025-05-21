package com.example.grow.data.repository

import com.example.grow.data.model.Resep
import com.example.grow.data.remote.ResepApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val apiService: ResepApiService // Gunakan jika server mendukung bookmark
) {
    private val _bookmarkedResepIds = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedResepIds: StateFlow<Set<String>> = _bookmarkedResepIds.asStateFlow()

    suspend fun toggleBookmark(resep: Resep) {
        val currentBookmarks = _bookmarkedResepIds.value.toMutableSet()
        if (currentBookmarks.contains(resep.idResep)) {
            currentBookmarks.remove(resep.idResep)
            // Jika server mendukung: apiService.removeBookmark(resep.idResep)
        } else {
            currentBookmarks.add(resep.idResep)
            // Jika server mendukung: apiService.addBookmark(resep.idResep)
        }
        _bookmarkedResepIds.value = currentBookmarks
    }

    fun isBookmarked(resepId: String): Boolean {
        return _bookmarkedResepIds.value.contains(resepId)
    }
}