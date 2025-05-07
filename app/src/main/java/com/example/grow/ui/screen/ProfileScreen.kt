package com.example.grow.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.grow.util.SessionManager
import com.example.grow.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(navController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Logout") },
            text = { Text("Apakah Anda yakin ingin logout?") },
            confirmButton = {
                Button(onClick = {
                    Log.d("ProfileScreen", "Before clear: isLoggedIn = ${SessionManager.isLoggedIn(context)}")
                    viewModel.logout(context) // Panggil logout dari AuthViewModel
                    Log.d("ProfileScreen", "After clear: isLoggedIn = ${SessionManager.isLoggedIn(context)}")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                    // Log back stack
                    navController.backQueue.forEachIndexed { index, entry ->
                        Log.d("ProfileScreen", "BackStack[$index]: ${entry.destination.route}")
                    }
                    showDialog = false
                }) {
                    Text("Ya")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Tidak")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profil",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Logout")
            }
        }
    }
}