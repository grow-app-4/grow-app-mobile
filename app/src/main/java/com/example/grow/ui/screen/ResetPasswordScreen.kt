package com.example.grow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.grow.ui.theme.PoppinsFamily
import com.example.grow.ui.theme.GROWTheme
import com.example.grow.viewmodel.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    email: String,
    resetToken: String,
    navController: NavHostController,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var isPasswordValid by remember { mutableStateOf(true) }

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

    // Tangani keberhasilan
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage == "Kata sandi berhasil direset.") {
            navController.navigate("password_reset_success")
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
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
                            text = "Reset Kata Sandi",
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
                        placeholder = { Text("Masukkan kata sandi baru", fontFamily = PoppinsFamily) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = !isPasswordValid && password.isNotEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = if (isPasswordValid) darkBlue else errorColor,
                            unfocusedContainerColor = Color(0xFFF8FAFD),
                            focusedContainerColor = Color(0xFFF8FAFD)
                        )
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
                        placeholder = { Text("Konfirmasi kata sandi baru", fontFamily = PoppinsFamily) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = !isPasswordValid && passwordConfirmation.isNotEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = if (isPasswordValid) darkBlue else errorColor,
                            unfocusedContainerColor = Color(0xFFF8FAFD),
                            focusedContainerColor = Color(0xFFF8FAFD)
                        )
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
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (isPasswordValid && password.isNotEmpty()) {
                                viewModel.resetPassword(email, resetToken, password, passwordConfirmation)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                        enabled = password.isNotEmpty() && passwordConfirmation.isNotEmpty() && isPasswordValid
                    ) {
                        Text(
                            text = if (uiState.isLoading) "Mereset..." else "Reset Kata Sandi",
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