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
class ResepViewModel @Inject constructor(
    private val resepRepository: ResepRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _resepList = MutableStateFlow<List<Resep>>(emptyList())
    val resepList: StateFlow<List<Resep>> = _resepList.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedTimeFilter = MutableStateFlow("All")
    val selectedTimeFilter: StateFlow<String> = _selectedTimeFilter.asStateFlow()

    private val _selectedRatingFilter = MutableStateFlow<Int?>(null)
    val selectedRatingFilter: StateFlow<Int?> = _selectedRatingFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    val bookmarkedResepIds = bookmarkRepository.bookmarkedResepIds

    init {
        loadResep()
    }

    fun loadResep() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val resepList = resepRepository.getAllResep()
                _resepList.value = resepList.map { resep ->
                    resep.copy(isBookmarked = bookmarkRepository.isBookmarked(resep.idResep))
                }
            } catch (e: Exception) {
                _error.value = "Gagal memuat resep: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun setFilters(timeFilter: String, ratingFilter: Int?, categoryFilter: String) {
        _selectedTimeFilter.value = timeFilter
        _selectedRatingFilter.value = ratingFilter
        _selectedCategoryFilter.value = categoryFilter
    }

    fun getFilteredResepList(searchQuery: String): List<Resep> {
        return _resepList.value.filter {
            val matchesSearch = searchQuery.isEmpty() ||
                    it.namaResep.contains(searchQuery, ignoreCase = true)

            val matchesTime = when (_selectedTimeFilter.value) {
                "Newest" -> true
                "Oldest" -> true
                "Popularity" -> it.rating?.let { r -> r >= 4.0 } ?: false
                else -> true
            }

            val matchesRating = _selectedRatingFilter.value?.let { rating ->
                it.rating?.toInt() == rating
            } ?: true

            val matchesCategory = _selectedCategoryFilter.value == "All" ||
                    it.namaKategori == _selectedCategoryFilter.value

            matchesSearch && matchesTime && matchesRating && matchesCategory
        }
    }

    fun toggleBookmark(resep: Resep) {
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(resep)
            _resepList.value = _resepList.value.map {
                if (it.idResep == resep.idResep) {
                    it.copy(isBookmarked = bookmarkRepository.isBookmarked(resep.idResep))
                } else {
                    it
                }
            }
        }
    }
}