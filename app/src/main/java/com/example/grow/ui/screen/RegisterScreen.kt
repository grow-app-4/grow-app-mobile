package com.example.grow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.grow.ui.theme.PoppinsFamily
import com.example.grow.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirmation by rememberSaveable { mutableStateOf("") }
    var isEmailValid by rememberSaveable { mutableStateOf(true) }
    var isPasswordValid by rememberSaveable { mutableStateOf(true) }
    val context = LocalContext.current

    // Warna
    val lightBlue = Color(0xFF47A9FF)
    val darkBlue = Color(0xFF1A73E8)
    val lighterBlue = Color(0xFF63B3FF)
    val errorColor = Color(0xFFD32F2F)

    // Gradient
    val gradient = Brush.verticalGradient(
        colors = listOf(lighterBlue, lightBlue),
        startY = 0f,
        endY = 1000f
    )

    // Navigasi setelah registrasi berhasil
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage == "success") {
            navController.navigate(Screen.Login.route) {
                popUpTo("register") { inclusive = true }
            }
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.35f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "grow.",
                        color = Color.White,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = PoppinsFamily
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Solusi Cerdas Deteksi\nDini Stunting",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        fontFamily = PoppinsFamily,
                        lineHeight = 28.sp
                    )
                }
            }

            // Kartu Form
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
                        .padding(top = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Bar Atas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.navigate(Screen.Login.route) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFF0F5FF), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Kembali",
                                tint = darkBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Registrasi",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3142),
                            fontFamily = PoppinsFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.5.dp,
                        color = Color.LightGray.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Kolom Nama
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        placeholder = { Text("Masukkan nama", fontFamily = PoppinsFamily) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(40.dp)
                                    .background(lightBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Ikon nama",
                                    tint = darkBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = darkBlue,
                            unfocusedContainerColor = Color(0xFFF8FAFD),
                            focusedContainerColor = Color(0xFFF8FAFD)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Kolom Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            isEmailValid = it.isEmpty() || (it.contains("@") && it.contains("."))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        placeholder = { Text("Masukkan email", fontFamily = PoppinsFamily) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(40.dp)
                                    .background(lightBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "Ikon email",
                                    tint = darkBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        isError = !isEmailValid && email.isNotEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = if (isEmailValid) darkBlue else errorColor,
                            unfocusedContainerColor = Color(0xFFF8FAFD),
                            focusedContainerColor = Color(0xFFF8FAFD)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    if (!isEmailValid && email.isNotEmpty()) {
                        Text(
                            text = "Masukkan email yang valid",
                            color = errorColor,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Kolom Kata Sandi
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            isPasswordValid = it.length >= 6 && it == passwordConfirmation
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        placeholder = { Text("Masukkan kata sandi", fontFamily = PoppinsFamily) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(40.dp)
                                    .background(lightBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Ikon kata sandi",
                                    tint = darkBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        isError = !isPasswordValid && password.isNotEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = if (isPasswordValid) darkBlue else errorColor,
                            unfocusedContainerColor = Color(0xFFF8FAFD),
                            focusedContainerColor = Color(0xFFF8FAFD)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Kolom Konfirmasi Kata Sandi
                    OutlinedTextField(
                        value = passwordConfirmation,
                        onValueChange = {
                            passwordConfirmation = it
                            isPasswordValid = it.length >= 6 && it == password
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        placeholder = { Text("Konfirmasi kata sandi", fontFamily = PoppinsFamily) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(40.dp)
                                    .background(lightBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Ikon konfirmasi kata sandi",
                                    tint = darkBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        isError = !isPasswordValid && passwordConfirmation.isNotEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = if (isPasswordValid) darkBlue else errorColor,
                            unfocusedContainerColor = Color(0xFFF8FAFD),
                            focusedContainerColor = Color(0xFFF8FAFD)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    if (!isPasswordValid && (password.isNotEmpty() || passwordConfirmation.isNotEmpty())) {
                        Text(
                            text = "Kata sandi harus sama dan minimal 6 karakter",
                            color = errorColor,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = errorColor,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tombol Registrasi
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && email.isNotEmpty() && isEmailValid && isPasswordValid) {
                                viewModel.register(name, email, password, passwordConfirmation, context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = darkBlue.copy(alpha = 0.4f)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                        enabled = name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() &&
                                passwordConfirmation.isNotEmpty() && isEmailValid && isPasswordValid
                    ) {
                        Text(
                            text = if (uiState.isLoading) "Mendaftar..." else "Daftar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFamily
                        )
                    }
                }
            }
        }
    }
}