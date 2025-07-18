package com.example.grow.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.grow.data.Resource
import com.example.grow.ui.screen.Screen
import com.example.grow.ui.theme.PoppinsFamily
import com.example.grow.ui.viewmodel.AnakViewModel
import com.example.grow.ui.viewmodel.PertumbuhanViewModel
import com.example.grow.util.SessionManager
import com.example.grow.viewmodel.AuthViewModel
import com.example.grow.viewmodel.GrafikViewModel
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel(),
    grafikViewModel: GrafikViewModel = hiltViewModel(),
    anakViewModel: AnakViewModel = hiltViewModel(),
    pertumbuhanViewModel: PertumbuhanViewModel = hiltViewModel()
) {
    // State declarations
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginMessage by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current
    val isLoggedIn by remember { mutableStateOf(SessionManager.isLoggedIn(context)) }

    // Theme colors
    val primaryColor = Color(0xFF1A73E8)
    val secondaryColor = Color(0xFF47A9FF)
    val lightColor = Color(0xFF63B3FF)
    val backgroundColor = Color(0xFFF7F9FC)
    val textPrimaryColor = Color(0xFF303030)
    val textSecondaryColor = Color(0xFF757575)

    // Gradient background
    val gradient = Brush.verticalGradient(
        colors = listOf(lightColor, secondaryColor),
        startY = 0f,
        endY = 1000f
    )

    // Login state effect
    LaunchedEffect(Unit) {
        if (!isLoggedIn) {
            viewModel.logout(context)
        }
    }

    // Handle login state changes
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is Resource.Loading -> {
                loginMessage = "Logging in..."
            }
            is Resource.Success -> {
                if (SessionManager.isLoggedIn(context)) {
                    loginMessage = "Login successful"
                    val userId = state.data?.user?.id
                    val token = state.data?.token

                    if (userId != null && token != null) {
                        SessionManager.saveLoginSession(context, userId, token)
                    }

                    try {
                        // Synchronize data on successful login
                        grafikViewModel.syncStandarPertumbuhan()
                        anakViewModel.fetchAllAnakFromApi(context)
                        pertumbuhanViewModel.loadDataAwal()
                    } catch (e: CancellationException) {
                        Log.d("LoginScreen", "Synchronization canceled: ${e.message}")
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Error during synchronization: ${e.message}")
                    }

                    // Navigate to Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is Resource.Error -> {
                loginMessage = state.message.toString()
            }
        }
    }

    // Main UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo section
            LogoSection()

            // Login form card
            LoginCard(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = it },
                onLoginClick = { viewModel.login(email, password, context) },
                onForgotPasswordClick = { navController.navigate("forgot_password") },
                onRegisterClick = { navController.navigate("register") },
                primaryColor = primaryColor,
                backgroundColor = backgroundColor
            )
        }
    }
}

@Composable
private fun LogoSection() {
    Box(
        modifier = Modifier
            .fillMaxHeight(0.4f)
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
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFamily
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Solusi Cerdas Deteksi\nDini Stunting",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                fontFamily = PoppinsFamily,
                lineHeight = 28.sp
            )
        }
    }
}

@Composable
private fun LoginCard(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    primaryColor: Color,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Login Header
            LoginHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // Email field
            InputField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "Enter your email",
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                primaryColor = primaryColor,
                backgroundColor = backgroundColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password field
            PasswordField(
                value = password,
                onValueChange = onPasswordChange,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = onPasswordVisibilityChange,
                primaryColor = primaryColor,
                backgroundColor = backgroundColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login button
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFamily
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onForgotPasswordClick) {
                    Text(
                        text = "Forgot password?",
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = PoppinsFamily
                    )
                }

                TextButton(onClick = onRegisterClick) {
                    Text(
                        text = "Register",
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = PoppinsFamily
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Login",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            fontFamily = PoppinsFamily
        )

        Spacer(modifier = Modifier.height(12.dp))

        Divider(
            modifier = Modifier.padding(horizontal = 32.dp),
            thickness = 1.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    primaryColor: Color,
    backgroundColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            fontFamily = PoppinsFamily
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(
                    placeholder,
                    fontFamily = PoppinsFamily,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = "$label icon",
                    tint = primaryColor
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f),
                focusedBorderColor = primaryColor,
                unfocusedContainerColor = backgroundColor,
                focusedContainerColor = backgroundColor
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    primaryColor: Color,
    backgroundColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Password",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            fontFamily = PoppinsFamily
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(
                    "Enter your password",
                    fontFamily = PoppinsFamily,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Password icon",
                    tint = primaryColor
                )
            },
            trailingIcon = {
                IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = primaryColor
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f),
                focusedBorderColor = primaryColor,
                unfocusedContainerColor = backgroundColor,
                focusedContainerColor = backgroundColor
            )
        )
    }
}