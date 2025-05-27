package com.example.grow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.Resep
import com.example.grow.data.repository.BookmarkRepository
import com.example.grow.data.repository.ResepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResepDetailViewModel @Inject constructor(
    private val resepRepository: ResepRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _resepDetail = MutableStateFlow<Resep?>(null)
    val resepDetail: StateFlow<Resep?> = _resepDetail.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val bookmarkedResepIds = bookmarkRepository.bookmarkedResepIds

    fun loadResepDetail(resepId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                // Gunakan method baru yang mendapatkan semua data sekaligus
                val resep = resepRepository.getResepDetail(resepId)
                _resepDetail.value = resep.copy(
                    isBookmarked = bookmarkRepository.isBookmarked(resepId)
                )
            } catch (e: Exception) {
                _error.value = "Error mengambil detail resep: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleBookmark(resep: Resep) {
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(resep)
            _resepDetail.value = _resepDetail.value?.copy(
                isBookmarked = bookmarkRepository.isBookmarked(resep.idResep)
            )
        }
    }
}