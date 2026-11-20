package com.example.myapplication.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.AboutScreen
import com.example.myapplication.ProfileScreen
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.remote.StudentRemoteDataSource
import com.example.myapplication.data.repository.DefaultStudentRepository
import com.example.myapplication.ui.detail.DetailViewModel
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.screen.DetailScreen
import com.example.myapplication.ui.screen.HomeScreen

@Composable
fun NavigationLabApp() {
    val remoteDataSource = remember {
        StudentRemoteDataSource(RetrofitClient.apiService)
    }
    val repository = remember {
        DefaultStudentRepository(remoteDataSource)
    }
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.factory(repository)
                )
                val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                val searchQuery by homeViewModel.searchQuery.collectAsStateWithLifecycle()

                HomeScreen(
                    uiState = homeUiState,
                    searchQuery = searchQuery,
                    onSearchQueryChange = homeViewModel::onSearchQueryChange,
                    onRefresh = homeViewModel::refresh,
                    onRetry = homeViewModel::retry,
                    onOpenDetail = { studentId ->
                        navController.navigate(Routes.detail(studentId))
                    },
                    onOpenProfile = {
                        navController.navigate(Routes.PROFILE)
                    },
                    onOpenAbout = {
                        navController.navigate(Routes.ABOUT)
                    }
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument("studentId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getInt("studentId") ?: -1
                val detailViewModel: DetailViewModel = viewModel(
                    factory = DetailViewModel.factory(repository)
                )
                val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(studentId) {
                    detailViewModel.loadStudent(studentId)
                }
                DetailScreen(
                    uiState = detailUiState,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ABOUT) {
                AboutScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
