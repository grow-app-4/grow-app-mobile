package com.example.grow.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.grow.ui.theme.BiruMudaSecondary
import com.example.grow.ui.viewmodel.AnakViewModel
import com.example.grow.ui.viewmodel.PertumbuhanViewModel
import com.example.grow.util.SessionManager
import com.example.grow.viewmodel.AuthViewModel
import com.example.grow.viewmodel.GrafikViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MainScreen(
    grafikViewModel: GrafikViewModel = hiltViewModel(),
    anakViewModel: AnakViewModel = hiltViewModel(),
    pertumbuhanViewModel: PertumbuhanViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedChild by pertumbuhanViewModel.selectedChild.collectAsState()
    val idAnak = selectedChild?.idAnak
    val context = LocalContext.current
    val isLoggedIn = SessionManager.isLoggedIn(context)

    // Daftar rute yang tidak menampilkan bottom bar
    val routesWithoutBottomBar = listOf(
        Screen.Splash.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.ForgotPassword.route,
        Screen.VerificationCode.route,
        Screen.ResetPassword.route,
        Screen.PasswordResetSuccess.route,
        Screen.TambahAnak.route,
        Screen.TambahKehamilan.route,
        Screen.TambahAsupan.route,
        Screen.InputDataPertumbuhan.route,
        Screen.EditDataPertumbuhan.route,
        Screen.EditAnak.route
    )

    // Jalankan sinkronisasi hanya jika pengguna sudah login dan bukan di splash screen
    LaunchedEffect(isLoggedIn, currentRoute) {
        if (isLoggedIn && currentRoute != Screen.Splash.route) {
            try {
                withContext(Dispatchers.IO) {
                    grafikViewModel.syncStandarPertumbuhan()
                    anakViewModel.fetchAllAnakFromApi(context)
                    pertumbuhanViewModel.loadDataAwal()
                }
                Log.d("MainScreen", "Data synchronization completed")
            } catch (e: Exception) {
                Log.e("MainScreen", "Sync error: ${e.message}")
            }
        }
    }

    Scaffold(
        containerColor = BiruMudaSecondary,
        bottomBar = {
            if (currentRoute !in routesWithoutBottomBar) {
                BottomNavigationWithFab(navController, currentRoute, idAnak)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavHost(navController, authViewModel)
        }
    }
}