package com.example.grow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.AppliedFilter
import com.example.grow.data.model.FilterCategory
import com.example.grow.data.model.Resep
import com.example.grow.data.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResepViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
    // Nantinya repository lain akan di-inject di sini
) : ViewModel() {

    private val _resepList = MutableStateFlow<List<Resep>>(emptyList())
    val resepList: StateFlow<List<Resep>> = _resepList.asStateFlow()

    private val _appliedFilters = MutableStateFlow<List<AppliedFilter>>(emptyList())
    val appliedFilters: StateFlow<List<AppliedFilter>> = _appliedFilters.asStateFlow()

    // Meneruskan bookmark dari repository
    val bookmarkedResepList = bookmarkRepository.bookmarkedResepList

    // Function untuk toggle bookmark
    fun toggleBookmark(resep: Resep) {
        bookmarkRepository.toggleBookmark(resep)
    }

    // Function untuk cek status bookmark
    fun isBookmarked(resepId: String): Boolean {
        return bookmarkRepository.isBookmarked(resepId)
    }

    // Function untuk memuat data dari API
    fun loadResep() {
        viewModelScope.launch {
            try {
                // Nantinya di sini akan memanggil repository untuk mendapatkan data dari API
                // Contoh: val resepList = resepRepository.getResepList()
                // _resepList.value = resepList
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun filterResep(
        searchQuery: String,
        timeFilter: String,
        ratingFilter: Int?,
        categoryFilter: String
    ): List<Resep> {
        return resepList.value.filter {
            val matchesSearch = searchQuery.isEmpty() || it.namaMakanan.contains(searchQuery, ignoreCase = true)
            val matchesTime = when (timeFilter) {
                "Newest" -> true // Logika untuk newest bisa ditambahkan
                "Oldest" -> true // Logika untuk oldest bisa ditambahkan
                "Popularity" -> true // Logika untuk popularity bisa ditambahkan
                else -> true
            }
            val matchesRating = ratingFilter?.let { rating -> it.rating?.toInt() == rating } ?: true
            val matchesCategory = categoryFilter == "All" || it.namaMakanan.contains(categoryFilter, ignoreCase = true)

            matchesSearch && matchesTime && matchesRating && matchesCategory
        }
    }

    // Function untuk filter dan search
    fun searchResep(query: String): List<Resep> {
        return _resepList.value.filter {
            it.namaMakanan.contains(query, ignoreCase = true)
        }
    }

    // Fungsi untuk menerapkan filter
    fun applyFilters(filterMap: Map<FilterCategory, List<String>>) {
        val newFilters = mutableListOf<AppliedFilter>()

        // Proses filter TIME
        filterMap[FilterCategory.TIME]?.forEach { timeValue ->
            if (timeValue != "All") {
                newFilters.add(
                    AppliedFilter(
                        category = FilterCategory.TIME,
                        value = timeValue,
                        displayText = when (timeValue) {
                            "Newest" -> "Terbaru"
                            "Oldest" -> "Terlama"
                            "Popularity" -> "Popularitas"
                            else -> timeValue
                        }
                    )
                )
            }
        }

        // Proses filter RATE
        filterMap[FilterCategory.RATE]?.forEach { rateValue ->
            newFilters.add(
                AppliedFilter(
                    category = FilterCategory.RATE,
                    value = rateValue,
                    displayText = "$rateValue ★"
                )
            )
        }

        // Proses filter CATEGORY
        filterMap[FilterCategory.CATEGORY]?.forEach { categoryValue ->
            if (categoryValue != "All") {
                newFilters.add(
                    AppliedFilter(
                        category = FilterCategory.CATEGORY,
                        value = categoryValue,
                        displayText = when (categoryValue) {
                            "Cereal" -> "Sereal"
                            "Vegetables" -> "Sayuran"
                            "Dinner" -> "Makan Malam"
                            "Chinese" -> "Cina"
                            "Local Dish" -> "Masakan Lokal"
                            "Fruit" -> "Buah"
                            "Breakfast" -> "Sarapan"
                            "Spanish" -> "Spanyol"
                            "Lunch" -> "Makan Siang"
                            else -> categoryValue
                        }
                    )
                )
            }
        }

        _appliedFilters.value = newFilters
    }

    // Fungsi untuk menghapus filter
    fun removeFilter(filter: AppliedFilter) {
        val currentFilters = _appliedFilters.value.toMutableList()
        currentFilters.remove(filter)
        _appliedFilters.value = currentFilters
    }

    // Fungsi untuk menerapkan filter ke daftar resep
    private fun applyFiltersToResepList() {
        val filters = _appliedFilters.value

        if (filters.isEmpty()) {
            // Jika tidak ada filter, kembalikan semua resep
            loadResep()
            return
        }

        // Gunakan dummyResepList sebagai contoh (nanti bisa diganti dengan API call)
        val filteredList = resepList.value.filter { resep ->
            var matchesTime = true
            var matchesRate = true
            var matchesCategory = true

            // Filter berdasarkan waktu (contoh implementasi, sesuaikan dengan kebutuhan)
            val timeFilters = filters.filter { it.category == FilterCategory.TIME }
            if (timeFilters.isNotEmpty()) {
                // Implementasi filter waktu di sini
                // Karena ini contoh, kita anggap semua resep cocok dengan filter waktu
                matchesTime = true
            }

            // Filter berdasarkan rating
            val rateFilters = filters.filter { it.category == FilterCategory.RATE }
            if (rateFilters.isNotEmpty()) {
                matchesRate = rateFilters.any { filter ->
                    resep.rating?.toInt() == filter.value.toInt()
                }
            }

            // Filter berdasarkan kategori (contoh implementasi, sesuaikan dengan kebutuhan)
            val categoryFilters = filters.filter { it.category == FilterCategory.CATEGORY }
            if (categoryFilters.isNotEmpty()) {
                // Karena tidak ada kategori dalam model Resep, kita perlu menambahkannya
                // Untuk contoh, kita anggap semua resep cocok dengan filter kategori
                matchesCategory = true
            }

            matchesTime && matchesRate && matchesCategory
        }

        _resepList.value = filteredList
    }
}