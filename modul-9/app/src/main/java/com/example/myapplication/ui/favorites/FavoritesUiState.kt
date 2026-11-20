package com.example.myapplication.ui.favorites

import com.example.myapplication.data.local.FavoriteStudentEntity

data class FavoritesUiState(
    val favorites: List<FavoriteStudentEntity> = emptyList(),
    val sortAscending: Boolean = true
)
