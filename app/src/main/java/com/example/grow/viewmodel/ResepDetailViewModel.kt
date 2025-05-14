package com.example.grow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.Resep
import com.example.grow.data.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResepDetailViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
    // Nantinya repository lain akan di-inject di sini
) : ViewModel() {

    private val _resepDetail = MutableStateFlow<Resep?>(null)
    val resepDetail: StateFlow<Resep?> = _resepDetail.asStateFlow()

    // Meneruskan bookmark dari repository
    val bookmarkedResepList = bookmarkRepository.bookmarkedResepList

    // Function untuk memuat detail resep berdasarkan ID
    fun loadResepDetail(resepId: String) {
        viewModelScope.launch {
            try {
                // Nantinya di sini akan memanggil repository untuk mendapatkan detail dari API
                // Contoh: val detail = resepRepository.getResepDetail(resepId)
                // _resepDetail.value = detail
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // Function untuk toggle bookmark
    fun toggleBookmark(resep: Resep) {
        bookmarkRepository.toggleBookmark(resep)
    }

    // Function untuk cek status bookmark
    fun isBookmarked(resepId: String): Boolean {
        return bookmarkRepository.isBookmarked(resepId)
    }
}