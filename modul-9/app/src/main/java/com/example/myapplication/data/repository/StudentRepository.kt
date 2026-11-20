package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Student

interface StudentRepository {
    suspend fun getStudents(): List<Student>
    suspend fun getStudent(id: Int): Student
    suspend fun searchStudents(query: String): List<Student>
}
