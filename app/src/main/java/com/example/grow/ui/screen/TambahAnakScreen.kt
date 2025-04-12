package com.example.grow.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import com.example.grow.ui.theme.*
import com.example.grow.ui.viewmodel.PertumbuhanViewModel
import java.util.Date
import android.app.DatePickerDialog
import android.widget.Toast
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahAnakScreen(
    navController: NavController,
    viewModel: PertumbuhanViewModel = hiltViewModel(),
    userId: Int
) {
    var namaAnak by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("") }
    var jenisKelamin by remember { mutableStateOf("") }
    var beratLahir by remember { mutableStateOf("") }
    var tinggiLahir by remember { mutableStateOf("") }
    var lingkarKepalaLahir by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Selected gender state
    var isLakiLakiSelected by remember { mutableStateOf(false) }
    var isPerempuanSelected by remember { mutableStateOf(false) }

    // Form validation
    val isFormValid = namaAnak.isNotBlank() &&
            tanggalLahir.isNotBlank() &&
            jenisKelamin.isNotBlank() &&
            beratLahir.isNotBlank() &&
            tinggiLahir.isNotBlank() &&
            lingkarKepalaLahir.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tambah Anak",
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
            // Nama Lengkap
            InputLabel(text = "Nama Lengkap")
            OutlinedTextField(
                value = namaAnak,
                onValueChange = { namaAnak = it },
                placeholder = { Text("Nama Lengkap") },
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
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tanggal Lahir section
            InputLabel(text = "Tanggal Lahir")
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
                                tanggalLahir = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
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
                    text = if (tanggalLahir.isNotEmpty()) tanggalLahir else "YYYY-MM-DD",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontSize = 14.sp,
                        color = if (tanggalLahir.isNotEmpty()) TextPrimary else TextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Jenis Kelamin
            InputLabel(text = "Jenis Kelamin")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Laki-laki button
                GenderSelectionButton(
                    text = "Laki - Laki",
                    isSelected = isLakiLakiSelected,
                    onClick = {
                        isLakiLakiSelected = true
                        isPerempuanSelected = false
                        jenisKelamin = "L"
                    },
                    modifier = Modifier.weight(1f)
                )

                // Perempuan button
                GenderSelectionButton(
                    text = "Perempuan",
                    isSelected = isPerempuanSelected,
                    onClick = {
                        isLakiLakiSelected = false
                        isPerempuanSelected = true
                        jenisKelamin = "P"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Berat Badan
            InputLabel(text = "Berat Badan Saat Lahir (Kg)")
            OutlinedTextField(
                value = beratLahir,
                onValueChange = { beratLahir = it },
                placeholder = { Text("Contoh: 3.5") },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Tinggi Badan
            InputLabel(text = "Tinggi Badan Saat Lahir (Cm)")
            OutlinedTextField(
                value = tinggiLahir,
                onValueChange = { tinggiLahir = it },
                placeholder = { Text("Contoh: 40.8") },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Lingkar Kepala
            InputLabel(text = "Lingkar Kepala Saat Lahir (Cm)")
            OutlinedTextField(
                value = lingkarKepalaLahir,
                onValueChange = { lingkarKepalaLahir = it },
                placeholder = { Text("Contoh: 40.8") },
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

            // Simpan Button
            Button(
                onClick = {
                    viewModel.addAnak(
                        nama = namaAnak,
                        tanggalLahir = tanggalLahir,
                        jenisKelamin = jenisKelamin,
                        beratBadan = beratLahir.toFloatOrNull() ?: 0f,
                        tinggiBadan = tinggiLahir.toFloatOrNull() ?: 0f,
                        lingkarKepala = lingkarKepalaLahir.toFloatOrNull() ?: 0f,
                        userId = userId,
                        onSuccess = {
                            Toast.makeText(context, "Data anak berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home")
                        },
                        onError = { error ->
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                            Log.e("TAG_ERROR", "Error occurred", error)
                        }
                    )
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
                enabled = isFormValid
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

@Composable
fun InputLabel(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = TextPrimary
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun GenderSelectionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isSelected) Blue else LightBlue)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Radio button circle
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(LightBlue)
                .border(
                    width = 2.dp,
                    color = if (isSelected) Color.White else Blue,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        // Text
        Text(
            text = text,
            style = TextStyle(
                fontFamily = PoppinsFamily,
                fontSize = 14.sp,
                color = if (isSelected) Color.White else TextPrimary
            )
        )
    }
}