package com.example.grow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.Resep
import com.example.grow.data.repository.ResepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ResepViewModel @Inject constructor(
    private val repository: ResepRepository
) : ViewModel() {

    val resepList: StateFlow<List<Resep>> = repository.resepList
    val bookmarkedResepIds: StateFlow<Set<String>> = repository.bookmarkedResepIds
    val loading: StateFlow<Boolean> = repository.loading
    val error: StateFlow<String?> = repository.error

    private val _selectedTimeFilter = MutableStateFlow("All")
    val selectedTimeFilter: StateFlow<String> = _selectedTimeFilter

    private val _selectedRatingFilter = MutableStateFlow<Int?>(null)
    val selectedRatingFilter: StateFlow<Int?> = _selectedRatingFilter

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter

    init {
        loadResep()
    }

    fun loadResep() {
        viewModelScope.launch {
            repository.loadResepList()
        }
    }

    fun toggleBookmark(resep: Resep) {
        repository.toggleBookmark(resep)
    }

    fun setFilters(time: String, rating: Int?, category: String) {
        Log.d("ResepViewModel", "Setting filters: time=$time, rating=$rating, category=$category")
        _selectedTimeFilter.value = time
        _selectedRatingFilter.value = rating
        _selectedCategoryFilter.value = category
    }

    fun getFilteredResepList(searchQuery: String): List<Resep> {
        Log.d("ResepViewModel", "Filters: time=${_selectedTimeFilter.value}, rating=${_selectedRatingFilter.value}, category=${_selectedCategoryFilter.value}, query=$searchQuery")
        var filteredList = resepList.value.filter {
            val matchesSearch = searchQuery.isEmpty() || it.namaResep.contains(searchQuery, ignoreCase = true)
            val matchesRating = _selectedRatingFilter.value?.let { rating ->
                it.rating?.let { r -> r >= rating.toFloat() && r < (rating + 1).toFloat() } ?: false
            } ?: true
            val matchesCategory = _selectedCategoryFilter.value == "All" || it.namaKategori == _selectedCategoryFilter.value
            Log.d("ResepViewModel", "Resep: ${it.namaResep}, matchesSearch=$matchesSearch, matchesRating=$matchesRating, matchesCategory=$matchesCategory")
            matchesSearch && matchesRating && matchesCategory
        }

        val formatter = DateTimeFormatter.ISO_DATE_TIME
        filteredList = when (_selectedTimeFilter.value) {
            "Popularity" -> filteredList.filter { it.rating?.let { r -> r >= 4.0 } ?: false }
            "Newest" -> filteredList.sortedByDescending { it.createdAt?.let { LocalDateTime.parse(it, formatter) } }
            "Oldest" -> filteredList.sortedBy { it.createdAt?.let { LocalDateTime.parse(it, formatter) } }
            else -> filteredList
        }

        Log.d("ResepViewModel", "Filtered list size: ${filteredList.size}")
        return filteredList
    }

    fun clearError() {
        repository.clearError()
    }
}