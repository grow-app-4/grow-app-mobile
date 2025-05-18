package com.example.grow.ui.screen

import com.example.grow.ui.components.rememberDatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.grow.viewmodel.KehamilanViewModel

@Composable
fun KehamilanScreen(
    navController: NavController,
    viewModel: KehamilanViewModel = hiltViewModel(),
    userId: Int
//    onNavigateToAsupan: (Int) -> Unit
) {
    val idUser = 1 // ganti jika perlu
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val response by viewModel.kehamilanResult.collectAsState()

    var tanggal by remember { mutableStateOf("") }
    var berat by remember { mutableStateOf("") }
    val datePickerDialog = rememberDatePickerDialog { selectedDate ->
        tanggal = selectedDate
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Input Kehamilan", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (tanggal.isNotEmpty()) tanggal else "Pilih tanggal awal kehamilan",
                modifier = Modifier
                    .weight(1f)
                    .clickable { datePickerDialog.show() },
                color = if (tanggal.isNotEmpty()) Color.Black else Color.Gray
            )
            Button(onClick = { datePickerDialog.show() }) {
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = berat,
            onValueChange = { berat = it },
            label = { Text("Berat Badan Saat Ini (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (tanggal.isNotEmpty() && berat.toFloatOrNull() != null) {
                    viewModel.tambahKehamilan(idUser, tanggal, berat.toFloat())
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan")
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }

        error?.let {
            Text("Error: $it", color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        response?.let {
            Text(
                "Berhasil menyimpan data: status = ${it.data.status}",
                color = Color.Green,
                modifier = Modifier.padding(top = 8.dp)
            )

//            Button(
//                onClick = {
//                    val userId = it.data.id_user
//                    onNavigateToAsupan(userId)
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 8.dp)
//            ) {
//                Text("Lanjut ke Input Asupan")
//            }
        }
    }
}