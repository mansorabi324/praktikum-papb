package com.example.myapplication.ui.home

import com.example.myapplication.data.model.Student

data class HomeUiState(
    val students: List<Student> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
