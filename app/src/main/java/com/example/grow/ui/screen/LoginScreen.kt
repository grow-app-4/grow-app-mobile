package com.example.grow.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.grow.data.Resource
import com.example.grow.util.SessionManager
import com.example.grow.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(navController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginMessage by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is Resource.Loading -> {
                loginMessage = "Logging in..."
            }
            is Resource.Success -> {
                loginMessage = "Login successful"
                // Simpan sesi login
                val userId = state.data?.user?.id
                if (userId != null) {
                    SessionManager.saveLoginSession(context, userId)
                }
                // Navigasi ke HomeScreen dan hapus LoginScreen dari back stack
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is Resource.Error -> {
                loginMessage = state.message.toString()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Button(
                onClick = { viewModel.login(email, password, context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Login")
            }

            if (loginMessage.isNotEmpty()) {
                Text(
                    text = loginMessage,
                    color = if (loginMessage.contains("Error", ignoreCase = true)) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { navController.navigate("forgot_password") }
            ) {
                Text("Forgot Password?")
            }

            TextButton(
                onClick = { navController.navigate("register") }
            ) {
                Text("Don't have an account? Register")
            }
        }
    }
}