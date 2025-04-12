package com.example.grow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.grow.ui.viewmodel.PertumbuhanViewModel
import com.example.grow.data.DetailPertumbuhanEntity
import com.example.grow.data.PertumbuhanEntity
import com.example.grow.data.model.DetailRequest
import com.example.grow.data.model.PertumbuhanRequest
import android.app.DatePickerDialog
import java.util.Calendar
import com.example.grow.ui.theme.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPertumbuhanScreen(
    navController: NavController,
    idAnak: Int,
    idPertumbuhan: Int,
    viewModel: PertumbuhanViewModel = hiltViewModel()
) {
    var tanggal by remember { mutableStateOf("") }
    var berat by remember { mutableStateOf("") }
    var tinggi by remember { mutableStateOf("") }
    var lingkar by remember { mutableStateOf("") }
    var isStunting by remember { mutableStateOf("tidak") }
    val context = LocalContext.current

    // Ambil data yang mau diedit
    LaunchedEffect(idPertumbuhan) {
        viewModel.getPertumbuhanById(idPertumbuhan)?.let { data ->
            tanggal = data.pertumbuhan.tanggalPencatatan
            isStunting = data.pertumbuhan.statusStunting
            berat = data.details.find { it.jenis.idJenis == 1 }?.detail?.nilai?.toString() ?: ""
            tinggi = data.details.find { it.jenis.idJenis == 2 }?.detail?.nilai?.toString() ?: ""
            lingkar = data.details.find { it.jenis.idJenis == 3 }?.detail?.nilai?.toString() ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Data Pertumbuhan",
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
                .padding(16.dp)
        ) {
            Text(
                text = "Tanggal Pertumbuhan",
                modifier = Modifier.padding(bottom = 8.dp),
                style = TextStyle(
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightBlue)
                    .clickable {
                        val calendar = Calendar.getInstance()
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                tanggal = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
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
                    text = if (tanggal.isNotEmpty()) tanggal else "DD-MM-YYYY",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontSize = 14.sp,
                        color = if (tanggal.isNotEmpty()) TextPrimary else TextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weight field
            GrowthDataField(
                label = "Berat Badan (kg)",
                placeholder = "Contoh: 3.5",
                value = berat,
                onValueChange = { berat = it },
                keyboardType = KeyboardType.Number
            )

            // Height field
            GrowthDataField(
                label = "Tinggi Badan (cm)",
                placeholder = "Contoh: 40.8",
                value = tinggi,
                onValueChange = { tinggi = it },
                keyboardType = KeyboardType.Number
            )

            // Head circumference field
            GrowthDataField(
                label = "Lingkar Kepala (cm)",
                placeholder = "Contoh: 40.8",
                value = lingkar,
                onValueChange = { lingkar = it },
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.weight(1f))

            // Submit button
            Button(
                onClick = {
                    val request = PertumbuhanRequest(
                        idAnak = idAnak,
                        tanggalPencatatan = tanggal,
                        statusStunting = isStunting,
                        details = listOf(
                            DetailRequest(2, berat.toFloatOrNull() ?: 0f),
                            DetailRequest(1, tinggi.toFloatOrNull() ?: 0f),
                            DetailRequest(3, lingkar.toFloatOrNull() ?: 0f)
                        )
                    )

                    // Pastikan ID pertumbuhan yang mau diupdate bukan 0
                    val entity = PertumbuhanEntity(
                        idPertumbuhan = idPertumbuhan,
                        idAnak = idAnak,
                        tanggalPencatatan = tanggal,
                        statusStunting = isStunting
                    )

                    val detailEntities = request.details.map {
                        DetailPertumbuhanEntity(
                            idPertumbuhan = idPertumbuhan,
                            idJenis = it.idJenis,
                            nilai = it.nilai
                        )
                    }

                    // Jalankan fungsi update
                    viewModel.updatePertumbuhan(entity, detailEntities)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue,
                    disabledContainerColor = Blue.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Simpan Data",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}