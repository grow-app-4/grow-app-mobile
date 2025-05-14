package com.example.grow.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.grow.util.SessionManager
import com.example.grow.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(navController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        Log.d("ProfileScreen", "Showing logout dialog")
        AlertDialog(
            onDismissRequest = {
                Log.d("ProfileScreen", "Dialog dismissed")
                showDialog = false
            },
            title = { Text("Logout") },
            text = { Text("Apakah Anda yakin ingin logout?") },
            confirmButton = {
                Button(onClick = {
                    Log.d("ProfileScreen", "Confirm logout clicked, isLoggedIn = ${SessionManager.isLoggedIn(context)}")
                    viewModel.logout(context)
                    Log.d("ProfileScreen", "After logout, isLoggedIn = ${SessionManager.isLoggedIn(context)}")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                    navController.backQueue.forEachIndexed { index, entry ->
                        Log.d("ProfileScreen", "BackStack[$index]: ${entry.destination.route}")
                    }
                    showDialog = false
                }) {
                    Text("Ya")
                }
            },
            dismissButton = {
                Button(onClick = {
                    Log.d("ProfileScreen", "Cancel logout clicked")
                    showDialog = false
                }) {
                    Text("Tidak")
                }
            }
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD6E7FF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.Center),
                        tint = Color(0xFF5C95FF)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Wulan bin Fulan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Text(
                    text = "wulan@example.com",
                    fontSize = 16.sp,
                    color = Color(0xFF2196F3)
                )

                IconButton(
                    onClick = { /* Handle edit profile */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = Color(0xFF2196F3)
                    )
                }
            }

            Text(
                text = "Profil",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ProfileMenuItem(
                title = "Kehamilan",
                onClick = { /* Navigate to pregnancy info */ }
            )

            ProfileMenuItem(
                title = "Data Anak",
                onClick = { /* Navigate to child data */ }
            )

            Text(
                text = "Pengaturan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ProfileMenuItem(
                title = "Favorit",
                onClick = { /* Navigate to favorites */ }
            )

            ProfileMenuItem(
                title = "Keluar",
                onClick = {
                    Log.d("ProfileScreen", "Keluar menu item clicked")
                    showDialog = true
                }
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = Color.Gray
            )
        }
    }
}