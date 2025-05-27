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

// Screen 1: Password Reset Success Screen
@Composable
fun PasswordResetSuccessScreen(
    navController: NavHostController
) {
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

            // Card containing the success message
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
                    // Header
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

                    // Success icon with circular background
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                color = lightBlue.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = lightBlue.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = darkBlue,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Success message
                    Text(
                        text = "Password Reset Successful",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3142),
                        fontFamily = PoppinsFamily,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Button to go to login
                    Button(
                        onClick = { navController.navigate(Screen.Login.route) },
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
                            text = "Kembali ke Login",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}