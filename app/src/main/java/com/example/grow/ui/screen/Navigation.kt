package com.example.grow.ui.screen

import HomeScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Nutrisi : Screen("nutrisi")
    object Resep : Screen("resep")
    object Profile : Screen("profile")
    object InputDataPertumbuhan : Screen("pertumbuhan/{idAnak}") {
        fun createRoute(idAnak: Int) = "pertumbuhan/$idAnak"
    }
    object EditDataPertumbuhan : Screen("pertumbuhan/{idAnak}/edit/{idPertumbuhan}") {
        fun createRoute(idAnak: Int, idPertumbuhan: Int) = "pertumbuhan/$idAnak/edit/$idPertumbuhan"
    }
    object TambahAnak : Screen("tambah_anak/{userId}") {
        fun createRoute(userId: Int) = "tambah_anak/$userId"
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController = navController, userId = 4) }
        composable(
            Screen.InputDataPertumbuhan.route,
            arguments = listOf(navArgument("idAnak") { type = NavType.IntType })
        ) { backStackEntry ->
            val idAnak = backStackEntry.arguments?.getInt("idAnak") ?: 0
            DataEntryScreen(navController = navController, idAnak = idAnak)
        }
        composable(
            route = Screen.EditDataPertumbuhan.route,
            arguments = listOf(
                navArgument("idAnak") { type = NavType.IntType },
                navArgument("idPertumbuhan") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val idAnak = backStackEntry.arguments?.getInt("idAnak") ?: 0
            val idPertumbuhan = backStackEntry.arguments?.getInt("idPertumbuhan") ?: 0
            EditPertumbuhanScreen(
                navController = navController,
                idAnak = idAnak,
                idPertumbuhan = idPertumbuhan
            )
        }
        composable(
            route = Screen.TambahAnak.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            TambahAnakScreen(navController = navController, userId = userId)
        }
    }
}