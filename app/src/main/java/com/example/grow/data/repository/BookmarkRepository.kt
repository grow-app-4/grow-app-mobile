package com.example.grow.data.repository

import com.example.grow.data.model.Resep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor() {

    private val _bookmarkedResepList = MutableStateFlow<List<Resep>>(emptyList())
    val bookmarkedResepList: StateFlow<List<Resep>> = _bookmarkedResepList.asStateFlow()

    fun toggleBookmark(resep: Resep) {
        val currentList = _bookmarkedResepList.value.toMutableList()
        if (isBookmarked(resep.idMakanan)) {
            currentList.removeIf { it.idMakanan == resep.idMakanan }
        } else {
            currentList.add(resep)
        }
        _bookmarkedResepList.value = currentList
    }

    fun isBookmarked(resepId: String): Boolean {
        return _bookmarkedResepList.value.any { it.idMakanan == resepId }
    }
}