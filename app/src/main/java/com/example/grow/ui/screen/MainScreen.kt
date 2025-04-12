package com.example.grow.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.grow.ui.viewmodel.AnakViewModel
import com.example.grow.ui.viewmodel.PertumbuhanViewModel
import com.example.grow.viewmodel.GrafikViewModel

@Composable
fun MainScreen(grafikViewModel: GrafikViewModel = hiltViewModel(),
               anakViewModel: AnakViewModel = hiltViewModel(),
               pertumbuhanViewModel: PertumbuhanViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        grafikViewModel.syncStandarPertumbuhan()
        anakViewModel.fetchAllAnakFromApi()
        pertumbuhanViewModel.loadDataAwal()
    }

    Scaffold(
        bottomBar = {
            BottomNavigationWithFab(navController, currentRoute, 1)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavHost(navController)
        }
    }
}
