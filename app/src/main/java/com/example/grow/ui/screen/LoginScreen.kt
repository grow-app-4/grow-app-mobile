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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginMessage by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current
    val isLoggedIn by remember { mutableStateOf(SessionManager.isLoggedIn(context)) }

    // Warna-warna yang digunakan
    val lightBlue = Color(0xFF47A9FF)
    val darkBlue = Color(0xFF1A73E8)
    val lighterBlue = Color(0xFF63B3FF)

    // Gradient untuk background
    val gradient = Brush.verticalGradient(
        colors = listOf(lighterBlue, lightBlue),
        startY = 0f,
        endY = 1000f
    )

    LaunchedEffect(Unit) {
        Log.d("Navigation", "Rendered LoginScreen2, isLoggedIn = $isLoggedIn")
        if (!isLoggedIn) {
            // Reset login state saat layar dirender setelah logout
            viewModel.logout(context)
        }
    }

    LaunchedEffect(loginState) {
        Log.d("LoginScreen", "loginState = $loginState")
        when (val state = loginState) {
            is Resource.Loading -> {
                loginMessage = "Logging in..."
            }
            is Resource.Success -> {
                if (SessionManager.isLoggedIn(context)) {
                    loginMessage = "Login successful"
                    val userId = state.data?.user?.id
                    if (userId != null) {
                        SessionManager.saveLoginSession(context, userId)
                    }
                    // Lakukan sinkronisasi hanya jika login berhasil
                    try {
                        Log.d("LoginScreen", "Memulai sinkronisasi data")
                        grafikViewModel.syncStandarPertumbuhan()
                        anakViewModel.fetchAllAnakFromApi()
                        pertumbuhanViewModel.loadDataAwal()
                        Log.d("LoginScreen", "Sinkronisasi data selesai")
                    } catch (e: CancellationException) {
                        Log.d("LoginScreen", "Sinkronisasi dibatalkan: ${e.message}")
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Error saat sinkronisasi: ${e.message}")
                    }
                    // Navigasi ke HomeScreen
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo dan nama aplikasi
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.45f)
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
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFamily
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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

            // Kartu login
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
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Login",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontFamily = PoppinsFamily
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Divider(
                        modifier = Modifier.padding(horizontal = 32.dp),
                        thickness = 1.dp,
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(30.dp))

                    // Form Email
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Email",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            fontFamily = PoppinsFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            placeholder = {
                                Text(
                                    "Enter your email",
                                    fontFamily = PoppinsFamily,
                                    color = Color.Gray.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "Email icon",
                                    tint = darkBlue
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f),
                                focusedBorderColor = darkBlue,
                                unfocusedContainerColor = Color(0xFFF7F9FC),
                                focusedContainerColor = Color(0xFFF7F9FC)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Form Password
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
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            placeholder = {
                                Text(
                                    "Enter your password",
                                    fontFamily = PoppinsFamily,
                                    color = Color.Gray.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Password icon",
                                    tint = darkBlue
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = darkBlue
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.7f),
                                focusedBorderColor = darkBlue,
                                unfocusedContainerColor = Color(0xFFF7F9FC),
                                focusedContainerColor = Color(0xFFF7F9FC)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(25.dp))

                    // Tombol Login
                    Button(
                        onClick = { viewModel.login(email, password, context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Forgot Password
                    TextButton(onClick = { navController.navigate("forgot_password") }) {
                        Text(
                            text = "Forgot password?",
                            color = darkBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = PoppinsFamily
                        )
                    }
                }
            }
        }
    }
}