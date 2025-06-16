package com.example.grow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.grow.data.Resource
import com.example.grow.data.model.AuthUiState
import com.example.grow.ui.theme.PoppinsFamily
import com.example.grow.viewmodel.AuthViewModel

@Composable
fun VerifyEmailScreen(
    email: String,
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val verifyEmailState by viewModel.verifyEmailState.collectAsState()
    var verificationCode = remember { mutableStateOf(List(5) { "" }) }
    val context = LocalContext.current

    // Warna
    val lightBlue = Color(0xFF47A9FF)
    val darkBlue = Color(0xFF1A73E8)
    val lighterBlue = Color(0xFF63B3FF)

    // Gradient
    val gradient = Brush.verticalGradient(
        colors = listOf(lighterBlue, lightBlue),
        startY = 0f,
        endY = 1000f
    )

    // Tangani keberhasilan verifikasi
    LaunchedEffect(verifyEmailState) {
        if (verifyEmailState is Resource.Success) {
            navController.navigate(Screen.Login.route) {
                popUpTo("register") { inclusive = true }
            }
            viewModel.clearMessages()
        }
    }

    // Tangani keberhasilan pengiriman ulang kode
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("Kode verifikasi telah dikirim ulang") == true) {
            // Tidak perlu navigasi, cukup tampilkan pesan
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
            // Logo and app name with drop shadow effect
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.45f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.graphicsLayer {
                        translationY = 0f
                    }
                ) {
                    Text(
                        text = "grow.",
                        color = Color.White,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = PoppinsFamily,
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = 0.99f
                            }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Solusi Cerdas Deteksi\nDini Stunting",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        fontFamily = PoppinsFamily,
                        lineHeight = 28.sp,
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = 0.95f
                            }
                    )
                }
            }

            // Card containing the verification code input
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
                        .padding(top = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { navController.navigate(Screen.Register.route) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFFF0F5FF),
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = darkBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Verifikasi Email",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3142),
                        fontFamily = PoppinsFamily
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.5.dp,
                        color = Color.LightGray.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Email instructions
                    Text(
                        text = "Cek email Anda di",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3142),
                        fontFamily = PoppinsFamily,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = email,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkBlue,
                        fontFamily = PoppinsFamily,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "untuk kode verifikasi email.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3142),
                        fontFamily = PoppinsFamily,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Verification code input fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 5) {
                            VerificationCodeDigit(
                                value = verificationCode.value[i],
                                onValueChange = { newValue ->
                                    if (newValue.length <= 1) {
                                        val newList = verificationCode.value.toMutableList()
                                        newList[i] = newValue
                                        verificationCode.value = newList
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Verification button
                    Button(
                        onClick = {
                            val code = verificationCode.value.joinToString("")
                            if (code.length == 5) {
                                viewModel.verifyEmailCode(email, code, context)
                            } else {
                                // Tampilkan pesan error jika kode tidak lengkap
                                viewModel.setErrorMessage("Masukkan kode 5 digit")
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkBlue
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            text = if (verifyEmailState is Resource.Loading) "Memverifikasi..." else "Verifikasi Kode",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFamily
                        )
                    }

                    // Resend code button
                    TextButton(
                        onClick = {
                            viewModel.resendVerificationCode(email)
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(
                            text = "Kirim Ulang Kode",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = darkBlue,
                            fontFamily = PoppinsFamily
                        )
                    }

                    // Error or success message
                    when {
                        verifyEmailState is Resource.Error -> {
                            (verifyEmailState as Resource.Error).message?.let {
                                Text(
                                    text = it,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                        uiState.errorMessage != null -> {
                            Text(
                                text = uiState.errorMessage!!,
                                color = Color(0xFFD32F2F),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        uiState.successMessage != null -> {
                            Text(
                                text = uiState.successMessage!!,
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}