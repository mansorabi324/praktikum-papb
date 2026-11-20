package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Student
import com.example.myapplication.data.remote.StudentRemoteDataSource
import com.example.myapplication.data.remote.dto.toStudent

class DefaultStudentRepository(
    private val remoteDataSource: StudentRemoteDataSource
) : StudentRepository {

    override suspend fun getStudents(): List<Student> =
        remoteDataSource.getStudents().map { it.toStudent() }

    override suspend fun getStudent(id: Int): Student =
        remoteDataSource.getStudent(id).toStudent()

    override suspend fun searchStudents(query: String): List<Student> =
        remoteDataSource.searchStudents(query).map { it.toStudent() }
}
