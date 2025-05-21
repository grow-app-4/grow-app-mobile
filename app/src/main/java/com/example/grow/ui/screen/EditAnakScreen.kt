package com.example.grow.ui.screen

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAnakScreen(
    navController: NavController,
    viewModel: PertumbuhanViewModel = hiltViewModel(),
    anakId: Int,
    userId: Int
) {
    var namaAnak by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("") }
    var jenisKelamin by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isLakiLakiSelected by remember { mutableStateOf(false) }
    var isPerempuanSelected by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isFormValid = namaAnak.isNotBlank() &&
            tanggalLahir.isNotBlank() &&
            jenisKelamin.isNotBlank()

    LaunchedEffect(Unit) {
        viewModel.getAnakById(
            anakId = anakId,
            userId = userId,
            context = context,
            onSuccess = { anak ->
                namaAnak = anak.namaAnak
                tanggalLahir = anak.tanggalLahir
                jenisKelamin = anak.jenisKelamin
                isLakiLakiSelected = anak.jenisKelamin == "L"
                isPerempuanSelected = anak.jenisKelamin == "P"
                isLoading = false
                Log.d("EditAnakScreen", "Data anak dimuat: $anak")
            },
            onError = { error ->
                Toast.makeText(context, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("EditAnakScreen", "Error memuat data anak", error)
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Data Anak",
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Blue)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
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

                InputLabel(text = "Tanggal Lahir")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50.dp))
                        .background(LightBlue)
                        .clickable {
                            val calendar = Calendar.getInstance()
                            val dateParts = tanggalLahir.split("-").map { it.toIntOrNull() ?: 0 }
                            val year = dateParts.getOrElse(0) { calendar.get(Calendar.YEAR) }
                            val month = dateParts.getOrElse(1) { calendar.get(Calendar.MONTH) } - 1
                            val day = dateParts.getOrElse(2) { calendar.get(Calendar.DAY_OF_MONTH) }
                            val datePicker = DatePickerDialog(
                                context,
                                { _, selectedYear, selectedMonth, selectedDay ->
                                    tanggalLahir = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                                },
                                year,
                                month,
                                day
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

                InputLabel(text = "Jenis Kelamin")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.updateAnak(
                            anakId = anakId,
                            nama = namaAnak,
                            tanggalLahir = tanggalLahir,
                            jenisKelamin = jenisKelamin,
                            userId = userId,
                            context = context,
                            onSuccess = {
                                Toast.makeText(context, "Data anak berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                Log.e("EditAnakScreen", "Error memperbarui data", error)
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
}