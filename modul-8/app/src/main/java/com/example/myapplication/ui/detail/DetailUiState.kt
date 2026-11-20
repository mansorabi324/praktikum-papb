package com.example.myapplication.ui.detail

import com.example.myapplication.data.model.Student

data class DetailUiState(
    val student: Student? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
