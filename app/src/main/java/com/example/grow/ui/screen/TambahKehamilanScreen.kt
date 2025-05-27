package com.example.grow.ui.screen

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.grow.viewmodel.KehamilanViewModel
import com.example.grow.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahKehamilanScreen(
    userId: Int,
    navController: NavController, // Added NavController as a parameter
    viewModel: KehamilanViewModel = hiltViewModel(),
    onNavigateToNutrisi: (Int) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val response by viewModel.kehamilanResult.collectAsState()
    val context = LocalContext.current

    var tanggal by remember { mutableStateOf("") }
    var berat by remember { mutableStateOf("") }

    // Form validation
    val isFormValid = tanggal.isNotBlank() && berat.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Input Kehamilan",
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
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Tanggal Awal Kehamilan section
            InputLabel(text = "Tanggal Awal Kehamilan")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(LightBlue)
                    .clickable {
                        val calendar = Calendar.getInstance()
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                tanggal = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePicker.show()
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = if (tanggal.isNotEmpty()) tanggal else "YYYY-MM-DD",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontSize = 14.sp,
                        color = if (tanggal.isNotEmpty()) TextPrimary else TextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Berat Badan
            InputLabel(text = "Berat Badan Saat Ini (Kg)")
            OutlinedTextField(
                value = berat,
                onValueChange = { berat = it },
                placeholder = { Text("Contoh: 65.5") },
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = Blue
                )
            }

            // Error message
            error?.let {
                Text(
                    "Error: $it",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontSize = 14.sp,
                        color = Color.Red
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Success message and navigation
            response?.let {
                Text(
                    "Berhasil menyimpan data: status = ${it.data.status}",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontSize = 14.sp,
                        color = Color.Green
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LaunchedEffect(response) {
                    val userId = it.data.id_user
                    onNavigateToNutrisi(userId)
                }
            }

            // Simpan Button
            if (response == null) {
                Button(
                    onClick = {
                        if (tanggal.isNotEmpty() && berat.toFloatOrNull() != null) {
                            viewModel.tambahKehamilan(userId, tanggal, berat.toFloat())
                        } else {
                            Toast.makeText(context, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        disabledContainerColor = Blue.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isFormValid && !isLoading
                ) {
                    Text(
                        "Simpan",
                        style = TextStyle(
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            // Home indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                )
            }
        }
    }
}