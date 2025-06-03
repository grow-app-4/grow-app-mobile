package com.example.grow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.model.Resep
import com.example.grow.data.repository.ResepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResepDetailViewModel @Inject constructor(
    private val repository: ResepRepository
) : ViewModel() {

    val resepDetail: StateFlow<Resep?> = repository.resepDetail
    val bookmarkedResepIds: StateFlow<Set<String>> = repository.bookmarkedResepIds
    val loading: StateFlow<Boolean> = repository.loading
    val error: StateFlow<String?> = repository.error

    fun loadResepDetail(id: String) {
        Log.d("ResepDetailViewModel", "loadResepDetail called with id: $id")
        viewModelScope.launch {
            repository.loadResepDetail(id)
        }
    }

    fun toggleBookmark(resep: Resep) {
        repository.toggleBookmark(resep)
    }
}