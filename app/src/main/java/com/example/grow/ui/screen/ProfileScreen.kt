package com.example.grow.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.grow.R
import com.example.grow.ui.viewmodel.ProfileUpdateViewModel
import com.example.grow.util.SessionManager
import com.example.grow.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileUpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userId = SessionManager.getUserId(context)
    val token = SessionManager.getToken(context)
    var showLogoutDialog by remember { mutableStateOf(false) }
    val user by viewModel.getUserById(userId).collectAsState(initial = null)
    val profileUiState by profileViewModel.uiState.collectAsState()

    LaunchedEffect(userId, token) {
        if (token != null && userId != 0) {
            profileViewModel.loadUserData(token, userId)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout(context)
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Ya")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Tidak")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
        ) {
            ProfileHeader(
                name = user?.name,
                email = user?.email,
                profileImageUri = profileUiState.profileImageUri?.toString(),
                onEditClick = { navController.navigate(Screen.ProfileUpdate.route) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileSection(
                title = "Profil",
                items = listOf(
                    ProfileItem("Kehamilan") {
                        if (userId > 0) {
                            navController.navigate(Screen.CatatanKehamilan.createRoute(userId))
                        }
                    },
                    ProfileItem("Data Anak") {
                        if (userId > 0) {
                            navController.navigate(Screen.ListDataAnak.createRoute(userId))
                        } else {
                            Log.e("ProfileScreen", "Invalid userId: $userId")
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSection(
                title = "Pengaturan",
                items = listOf(
                    ProfileItem("Favorit") { navController.navigate(Screen.BookmarkResep.route) },
                    ProfileItem("Keluar") { showLogoutDialog = true }
                )
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String?,
    email: String?,
    profileImageUri: String?,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD6E7FF))
            ) {
                if (profileImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUri),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(65.dp)
                            .align(Alignment.Center),
                        tint = Color(0xFF5C95FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name ?: "Memuat nama...",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Text(
                text = email ?: "Memuat email...",
                fontSize = 16.sp,
                color = Color(0xFF2196F3),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEBF5FF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

data class ProfileItem(val title: String, val onClick: () -> Unit)

@Composable
private fun ProfileSection(
    title: String,
    items: List<ProfileItem>
) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp),
        color = Color(0xFF333333)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                ProfileMenuItem(
                    title = item.title,
                    onClick = item.onClick,
                    showDivider = index < items.size - 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Column {
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
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Navigate",
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (showDivider) {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFEEEEEE),
                    thickness = 1.dp
                )
            }
        }
    }
}
