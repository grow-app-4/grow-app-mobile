package com.example.grow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.grow.ui.screen.AsupanScreen
import com.example.grow.ui.screen.KehamilanScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "kehamilan") {
        composable("kehamilan") {
            KehamilanScreen(
                onNavigateToAsupan = { userId ->
                    navController.navigate("asupan/$userId")
                }
            )
        }
        composable("asupan/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 1
            AsupanScreen(idUser = userId)
        }
    }
}
