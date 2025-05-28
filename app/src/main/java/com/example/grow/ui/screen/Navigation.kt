package com.example.grow.ui.screen

import ForgotPasswordScreen
import HomeScreen
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Nutrisi : Screen("nutrisi")
    object Resep : Screen("resep")
    object Profile : Screen("profile")
    object BookmarkResep : Screen("bookmark_resep")
    object ProfileUpdate : Screen("profile_update")
    object InputDataPertumbuhan : Screen("pertumbuhan/{idAnak}") {
        fun createRoute(idAnak: Int) = "pertumbuhan/$idAnak"
    }
    object EditDataPertumbuhan : Screen("pertumbuhan/{idAnak}/edit/{idPertumbuhan}") {
        fun createRoute(idAnak: Int, idPertumbuhan: Int) = "pertumbuhan/$idAnak/edit/$idPertumbuhan"
    }
    object TambahAnak : Screen("tambah_anak/{userId}") {
        fun createRoute(userId: Int) = "tambah_anak/$userId"
    }
    object TambahKehamilan : Screen("tambah_kehamilan/{userId}") {
        fun createRoute(userId: Int) = "tambah_kehamilan/$userId"
    }
    object TambahAsupan : Screen("asupan_screen/{idUser}/{tanggalKonsumsi}") {
        fun createRoute(idUser: Int, tanggalKonsumsi: String) =
            "asupan_screen/$idUser/$tanggalKonsumsi"
    }
    object EditAnak : Screen("edit_anak/{userId}/{anakId}") {
        fun createRoute(userId: Int, anakId: Int) = "edit_anak/$userId/$anakId"
    }
    object ResepDetail : Screen("resep_detail/{resepId}") {
        fun createRoute(resepId: String) = "resep_detail/$resepId"
    }
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object VerificationCode : Screen("verification_code/{email}") {
        fun createRoute(email: String) = "verification_code/$email"
    }
    object ResetPassword : Screen("reset_password/{email}/{resetToken}") {
        fun createRoute(email: String, resetToken: String) = "reset_password/$email/$resetToken"
    }
    object PasswordResetSuccess : Screen("password_reset_success")
    object ListDataAnak : Screen("list_data_anak/{userId}") {
        fun createRoute(userId: Int) = "list_data_anak/$userId"
    }
}

@Composable
fun AppNavHost(navController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                onSplashFinished = { isLoggedIn ->
                    navController.navigate(if (isLoggedIn) Screen.Home.route else Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, userId = SessionManager.getUserId(LocalContext.current))
        }
        composable(Screen.Nutrisi.route) {
            val userId = SessionManager.getUserId(LocalContext.current)
            NutrisiScreen(navController = navController, userId = userId)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.ProfileUpdate.route) {
            ProfileUpdateScreen(navController = navController)
        }
        composable(Screen.Resep.route) {
            ResepScreen(navController = navController)
        }
        composable(Screen.BookmarkResep.route) {
            BookmarkResepScreen(navController = navController)
        }
        composable(
            route = Screen.ResepDetail.route,
            arguments = listOf(navArgument("resepId") { type = NavType.StringType })
        ) { backStackEntry ->
            val resepId = backStackEntry.arguments?.getString("resepId") ?: ""
            ResepDetailScreen(
                resepId = resepId,
                navController = navController
            )
        }
        composable(
            route = Screen.TambahKehamilan.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            TambahKehamilanScreen(
                userId = userId,
                navController = navController,
                onNavigateToNutrisi = { navigatedUserId ->
                    navController.navigate(Screen.Nutrisi.route)
                }
            )
        }
        composable(
            route = Screen.TambahAsupan.route,
            arguments = listOf(
                navArgument("idUser") { type = NavType.IntType },
                navArgument("tanggalKonsumsi") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val idUser = backStackEntry.arguments?.getInt("idUser") ?: 0
            val tanggalKonsumsi = backStackEntry.arguments?.getString("tanggalKonsumsi") ?: ""
            TambahAsupanScreen(
                idUser = idUser,
                tanggalKonsumsi = tanggalKonsumsi,
                navController = navController
            )
        }
        composable(
            route = Screen.InputDataPertumbuhan.route,
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
        composable(
            route = Screen.ListDataAnak.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            ListDataAnakScreen(navController = navController, userId = userId)
        }
        composable(
            route = Screen.EditAnak.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("anakId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val anakId = backStackEntry.arguments?.getInt("anakId") ?: 0
            UpdateDataAnakScreen(navController = navController, userId = userId, anakId = anakId)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, viewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }
        composable(
            route = Screen.VerificationCode.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerificationCodeScreen(email = email, navController = navController)
        }
        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("resetToken") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val resetToken = backStackEntry.arguments?.getString("resetToken") ?: ""
            ResetPasswordScreen(email = email, resetToken = resetToken, navController = navController)
        }
        composable(Screen.PasswordResetSuccess.route) {
            PasswordResetSuccessScreen(navController)
        }
    }
}