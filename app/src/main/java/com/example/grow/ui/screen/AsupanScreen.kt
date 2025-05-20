package com.example.grow.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import com.example.grow.model.Makanan
import com.example.grow.viewmodel.AsupanViewModel
import com.example.grow.enu.KategoriMakanan

@Composable
fun AsupanScreen(
    idUser: Int,
    tanggalKonsumsi: String,
    navController: NavController,
    viewModel: AsupanViewModel = hiltViewModel()
) {
    val makananPerKategori by viewModel.makananGrouped.collectAsState()
    val selectedMakanan by viewModel.selectedMakanan.collectAsState()
    val result by viewModel.result.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState()}
    var showNavigateButton by remember { mutableStateOf(false) }

    var currentStepIndex by remember { mutableStateOf(0) }
    val kategoriList = KategoriMakanan.ordered
    val currentKategori = kategoriList[currentStepIndex]
    val listMakanan = makananPerKategori[currentKategori.label] ?: emptyList()

    LaunchedEffect(Unit) {
        viewModel.loadMakananIbuHamil()
    }

    // Tampilkan snackbar saat result pertama kali muncul
    LaunchedEffect(result) {
        result?.let {
            snackbarHostState.showSnackbar("Analisis berhasil! Silakan lihat grafik hasil.")
            showNavigateButton = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(16.dp)) {

            Text("Kategori: ${currentKategori.label}", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(listMakanan) { makanan ->
                    val currentPorsi = selectedMakanan[makanan.id_makanan] ?: 0
                    var inputText by remember(makanan.id_makanan) { mutableStateOf(currentPorsi.toString()) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(makanan.nama_makanan, style = MaterialTheme.typography.bodyLarge)
                            Text("Ukuran: ${makanan.ukuran_porsi_umpama}")
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { newText ->
                                    inputText = newText
                                    val jumlah = newText.toIntOrNull() ?: 0
                                    viewModel.pilihMakanan(makanan.id_makanan, jumlah)
                                },
                                label = { Text("Jumlah Porsi") },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStepIndex > 0) {
                    Button(onClick = { currentStepIndex-- }) {
                        Text("Kembali")
                    }
                }

                if (currentStepIndex < kategoriList.lastIndex) {
                    Button(onClick = { currentStepIndex++ }) {
                        Text("Lanjut")
                    }
                } else {
                    Button(onClick = { viewModel.kirimAnalisis(idUser, tanggalKonsumsi) }) {
                        Text("Kirim")
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp))
            }

            result?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text("✅ Analisis berhasil dilakukan.", color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        navController.navigate("beranda/$idUser") {
                            popUpTo("asupan_screen/$idUser/$tanggalKonsumsi") { inclusive = true }
                        }
                    }
                ) {
                    Text("Lihat Grafik Hasil")
                }
            }
        }
    }
}
