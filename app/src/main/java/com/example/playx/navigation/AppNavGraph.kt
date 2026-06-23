package com.example.playx.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.playx.ui.screen.ConfigScreen
import com.example.playx.ui.screen.MainScreen
import com.example.playx.ui.viewmodel.ConfigViewModel
import com.example.playx.ui.viewmodel.MainViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val mainViewModel: MainViewModel = viewModel()
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToConfig = { navController.navigate("config") }
            )
        }
        composable("config") {
            val configViewModel: ConfigViewModel = viewModel()
            ConfigScreen(
                viewModel = configViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}