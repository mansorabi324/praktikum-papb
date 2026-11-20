package com.example.myapplication.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.preferences.UserPreferencesRepository
import com.example.myapplication.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                favoriteRepository.observeFavorites(),
                preferencesRepository.sortAscending
            ) { favorites, sortAscending ->
                val sorted = if (sortAscending) {
                    favorites.sortedBy { it.firstName.lowercase() }
                } else {
                    favorites.sortedByDescending { it.firstName.lowercase() }
                }
                FavoritesUiState(
                    favorites = sorted,
                    sortAscending = sortAscending
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleSortOrder() {
        viewModelScope.launch {
            preferencesRepository.saveSortAscending(!_uiState.value.sortAscending)
        }
    }

    companion object {
        fun factory(
            favoriteRepository: FavoriteRepository,
            preferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FavoritesViewModel(
                        favoriteRepository,
                        preferencesRepository
                    ) as T
                }
            }
    }
}
