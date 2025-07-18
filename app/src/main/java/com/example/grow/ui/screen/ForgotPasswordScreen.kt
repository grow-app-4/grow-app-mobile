import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.grow.ui.screen.Screen
import com.example.grow.ui.theme.PoppinsFamily
import com.example.grow.ui.theme.GROWTheme
import com.example.grow.viewmodel.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }

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
        if (uiState.successMessage == "Kode reset telah dikirim ke email Anda.") {
            navController.navigate("verification_code/$email")
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
            // Logo dan nama aplikasi dengan efek drop shadow
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

            // Kartu forgot password dengan efek shadow yang lebih halus
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
                    // Top bar with back button - stylized
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
                                .background(
                                    color = Color(0xFFF0F5FF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = darkBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Forgot Password",
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

                    Spacer(modifier = Modifier.height(22.dp))

                    // Instruction text in a card for better visibility
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        color = Color(0xFFF0F8FF),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Reset Your Password",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = darkBlue,
                                fontFamily = PoppinsFamily,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Enter your email address below and we'll send you a link to reset your password.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.DarkGray,
                                fontFamily = PoppinsFamily,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Form Email dengan icon dan validasi
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Email",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D3142),
                                fontFamily = PoppinsFamily
                            )

                            if (!isEmailValid && email.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• Masukkan email yang valid",
                                    color = errorColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = PoppinsFamily
                                )
                            }
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                isEmailValid = it.isEmpty() || (it.contains("@") && it.contains("."))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = Color.Black.copy(alpha = 0.1f)
                                ),
                            placeholder = {
                                Text(
                                    "Enter your email",
                                    fontFamily = PoppinsFamily,
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    fontSize = 16.sp
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .size(40.dp)
                                        .background(
                                            color = lightBlue.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Email,
                                        contentDescription = "Email icon",
                                        tint = darkBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = if (isEmailValid) darkBlue else errorColor,
                                unfocusedContainerColor = Color(0xFFF8FAFD),
                                focusedContainerColor = Color(0xFFF8FAFD)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = !isEmailValid && email.isNotEmpty() || uiState.errorMessage != null
                        )

                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = errorColor,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tombol Submit dengan efek gradien dan elevation
                    Button(
                        onClick = {
                            if (email.isNotEmpty() && isEmailValid) {
                                viewModel.sendForgotPassword(email)
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
                            containerColor = darkBlue,
                            disabledContainerColor = darkBlue.copy(alpha = 0.6f)
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        enabled = email.isNotEmpty() && isEmailValid
                    ) {
                        Text(
                            text = "Submit",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFamily,
                            modifier = Modifier.animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}