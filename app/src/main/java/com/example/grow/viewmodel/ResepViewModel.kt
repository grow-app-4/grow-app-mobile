package com.example.grow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.Resep
import com.example.grow.data.repository.ResepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
        _selectedTimeFilter.value = time
        _selectedRatingFilter.value = rating
        _selectedCategoryFilter.value = category
    }

    fun getFilteredResepList(searchQuery: String): List<Resep> {
        return resepList.value.filter {
            (searchQuery.isEmpty() || it.namaResep.contains(searchQuery, ignoreCase = true)) &&
                    (_selectedTimeFilter.value == "All" || (_selectedTimeFilter.value == "Popularity" && it.rating?.let { r -> r >= 4.0 } ?: false)) &&
                    (_selectedRatingFilter.value?.let { rating -> it.rating?.toInt() == rating } ?: true) &&
                    (_selectedCategoryFilter.value == "All" || it.namaKategori == _selectedCategoryFilter.value)
        }
    }
}