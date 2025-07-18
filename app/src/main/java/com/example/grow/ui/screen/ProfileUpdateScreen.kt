package com.example.grow.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.grow.R
import com.example.grow.ui.theme.*
import com.example.grow.ui.viewmodel.ProfileUpdateViewModel
import com.example.grow.util.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileUpdateScreen(
    navController: NavController,
    viewModel: ProfileUpdateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val userId = SessionManager.getUserId(context)
    val token = SessionManager.getToken(context)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateProfileImage(uri)
    }

    LaunchedEffect(userId, token) {
        if (token != null && userId != 0) {
            viewModel.loadUserData(token, userId)
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Token atau User ID tidak valid, silakan login ulang")
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    when {
                        message.contains("timeout") -> "Gagal terhubung ke server, data disimpan lokal"
                        message.contains("ID pengguna tidak valid") -> "Sesi tidak valid, silakan login ulang"
                        message.contains("Tidak ada perubahan") -> "Tidak ada perubahan untuk disimpan"
                        message.contains("Data pengguna belum dimuat") -> "Data pengguna belum dimuat"
                        else -> message
                    }
                )
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            navController.navigateUp()
            viewModel.resetUpdateSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Perbarui Profil",
                        style = TextStyle(
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.originalUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Profile Picture
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(LightBlue)
                            .clickable { pickImageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.profileImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(uiState.profileImageUri),
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_star),
                                contentDescription = "Default User Avatar",
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        IconButton(
                            onClick = { pickImageLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Blue)
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile Picture",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (uiState.profileImageUri != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.updateProfileImage(null) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .border(2.dp, Color.White, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Profile Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Form Fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    InputLabel(text = "Nama")
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        placeholder = { Text("Nama Lengkap") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                            .background(LightBlue),
                        shape = RoundedCornerShape(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = LightBlue,
                            unfocusedContainerColor = LightBlue
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    InputLabel(text = "Email")
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        placeholder = { Text("mail@mail.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                            .background(LightBlue),
                        shape = RoundedCornerShape(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = LightBlue,
                            unfocusedContainerColor = LightBlue
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (token != null) {
                            viewModel.updateUser(token, userId, context)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Token tidak tersedia, silakan login ulang")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading && uiState.originalUser != null
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Perbarui",
                            style = TextStyle(
                                fontFamily = PoppinsFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }
        }
    }
}