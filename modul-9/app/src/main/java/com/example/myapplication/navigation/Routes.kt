package com.example.myapplication.navigation

object Routes {
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"
    const val ABOUT = "about"
    const val DETAIL = "detail/{studentId}"

    fun detail(studentId: Int): String = "detail/$studentId"
}
