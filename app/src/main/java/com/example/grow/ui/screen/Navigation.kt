package com.example.grow.ui.screen

import ForgotPasswordScreen
import HomeScreen
import LoginScreen
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.grow.util.SessionManager
import com.example.grow.viewmodel.AuthViewModel

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
    object Login : Screen("login")
}

@Composable
fun AppNavHost(navController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val isLoggedIn = remember { mutableStateOf(SessionManager.isLoggedIn(context)) }

    // Pantau perubahan isLoggedIn
    LaunchedEffect(Unit) {
        snapshotFlow { SessionManager.isLoggedIn(context) }
            .collect { newIsLoggedIn ->
                Log.d("AppNavHost", "isLoggedIn changed to $newIsLoggedIn")
                isLoggedIn.value = newIsLoggedIn
            }
    }

    // Navigasi berdasarkan status login
    LaunchedEffect(isLoggedIn.value) {
        Log.d("AppNavHost", "LaunchedEffect: isLoggedIn = ${isLoggedIn.value}")
        val destination = if (isLoggedIn.value) Screen.Home.route else Screen.Login.route
        navController.navigate(destination) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, userId = SessionManager.getUserId(context))
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
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
        composable(Screen.Login.route) {
            LoginScreen(navController, viewModel)
        }
        composable("register") { RegisterScreen() }
        composable("forgot_password") { ForgotPasswordScreen(navController) }
    }
}