package com.example.grow.ui.screen

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
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.grow.ui.viewmodel.PertumbuhanViewModel
import com.example.grow.data.JenisPertumbuhanEntity
import com.example.grow.data.PertumbuhanEntity
import com.example.grow.data.model.DetailRequest
import com.example.grow.data.model.PertumbuhanRequest
import com.example.grow.ui.theme.*
import android.app.DatePickerDialog
import android.widget.Toast
import java.util.Calendar
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEntryScreen(
    navController: NavController,
    idAnak: Int,
    viewModel: PertumbuhanViewModel = hiltViewModel()
) {
    var tanggal by remember { mutableStateOf("") }
    var berat by remember { mutableStateOf("") }
    var tinggi by remember { mutableStateOf("") }
    var lingkar by remember { mutableStateOf("") }
    var isStunting by remember { mutableStateOf("tidak") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tambah Data Pertumbuhan",
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
                            DetailRequest(1, berat.toFloatOrNull() ?: 0f),
                            DetailRequest(2, tinggi.toFloatOrNull() ?: 0f),
                            DetailRequest(3, lingkar.toFloatOrNull() ?: 0f)
                        )
                    )

                    val entity = PertumbuhanEntity(0, idAnak, tanggal, isStunting)

                    val jenisList = listOf(
                        JenisPertumbuhanEntity(2, "Berat Badan"),
                        JenisPertumbuhanEntity(1, "Tinggi Badan"),
                        JenisPertumbuhanEntity(3, "Lingkar Kepala")
                    )

                    viewModel.createPertumbuhan(request, entity, jenisList)

                    Toast.makeText(context, "Data berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthDataField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.padding(bottom = 8.dp),
            style = TextStyle(
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextPrimary
            )
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(LightBlue),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = PoppinsFamily,
                fontSize = 14.sp,
                color = TextPrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = LightBlue,
                unfocusedContainerColor = LightBlue,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorContainerColor = LightBlue
            )
        )
    }
}