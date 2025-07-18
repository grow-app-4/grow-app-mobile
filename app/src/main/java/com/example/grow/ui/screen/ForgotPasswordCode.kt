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

// Screen 2: Verification Code Screen
@Composable
fun VerificationCodeScreen(
    email: String,
    navController: NavHostController,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var verificationCode = remember { mutableStateOf(List(5) { "" }) }

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

    // Tangani keberhasilan
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage == "Kode berhasil diverifikasi." && uiState.resetToken != null) {
            navController.navigate("reset_password/$email/${uiState.resetToken}")
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
                        onClick = { navController.navigate(Screen.ForgotPassword.route) },
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
                        text = "Forgot Password",
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
                        text = "Check your email at",
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
                        text = "for a password reset link.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3142),
                        fontFamily = PoppinsFamily,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

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

                    Spacer(modifier = Modifier.height(28.dp))

                    // Verification button
                    Button(
                        onClick = {
                            val code = verificationCode.value.joinToString("")
                            if (code.length == 5) {
                                viewModel.verifyResetCode(email, code)
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
                            text = if (uiState.isLoading) "Memverifikasi..." else "Verifikasi Kode",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFamily
                        )
                    }

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun VerificationCodeDigit(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lightGray = Color(0xFFEEEEEE)
    val darkBlue = Color(0xFF1A73E8)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3142),
            fontFamily = PoppinsFamily,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
            .size(70.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(lightGray.copy(alpha = 0.3f)),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                // Show the dash when empty
                if (value.isEmpty()) {
                    Text(
                        text = "—",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        }
    )
}