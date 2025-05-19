package com.example.grow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.grow.ui.screen.AsupanScreen
import com.example.grow.ui.screen.BerandaScreen
import com.example.grow.ui.screen.KehamilanScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "beranda") {
        composable("kehamilan") {
            KehamilanScreen(
                onNavigateToBeranda = { userId ->
                    navController.navigate("beranda/$userId") {
                        popUpTo("kehamilan") { inclusive = true }
                    }
                }
            )
        }

        composable("asupan_screen/{userId}/{tanggalKonsumsi}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: return@composable
            val tanggalKonsumsi = backStackEntry.arguments?.getString("tanggalKonsumsi") ?: return@composable
            AsupanScreen(idUser = userId, tanggalKonsumsi = tanggalKonsumsi, navController = navController)
        }

        composable("beranda/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 1
            BerandaScreen(userId = userId, navController = navController)
        }

        // Tambahan: route statis awal
        composable("beranda") {
            // Navigasi langsung ke userId default (misal: 1)
            LaunchedEffect(Unit) {
                navController.navigate("beranda/1") {
                    popUpTo("beranda") { inclusive = true }
                }
            }
        }
    }
}
