package com.example.grow.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.grow.model.Anak
import com.example.grow.model.Makanan
import com.example.grow.viewmodel.AnakViewModel
import com.example.grow.viewmodel.MakananAnakViewModel
import com.example.grow.viewmodel.MakananViewModel
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    makananViewModel: MakananViewModel = hiltViewModel(),
    anakViewModel: AnakViewModel = hiltViewModel(),
    makananAnakViewModel: MakananAnakViewModel = hiltViewModel()
) {
    // Scaffold untuk mengontrol Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Ambil data makanan dan anak
    val makananList by makananViewModel.makananStateFlow.collectAsState(initial = emptyList())
    val anakList by anakViewModel.anakStateFlow.collectAsState(initial = emptyList())

    // Menangani input untuk porsi dan mengirim data
    var selectedAnakId by remember { mutableStateOf<Long?>(null) }
    var selectedMakananId by remember { mutableStateOf<Long?>(null) }
    var porsi by remember { mutableStateOf("0") }

    // Mengambil data makanan dan anak
    LaunchedEffect(Unit) {
        makananViewModel.getMakanan()
        anakViewModel.getAnak()
    }

    // Observer untuk menampilkan Snackbar berdasarkan status dari ViewModel
    val toastMessage by makananAnakViewModel.toastMessage.collectAsState()
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            // Menampilkan snackbar saat pesan berubah
            snackbarHostState.showSnackbar(it)
        }
    }

    // Scaffold dengan content utama
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // Menyediakan state untuk Scaffold dan Snackbar
        topBar = {
            TopAppBar(
                title = { Text("Data Makanan dan Anak") }
            )
        },
        content = { innerPadding ->
            MainContent(
                makananList = makananList,
                anakList = anakList,
                selectedAnakId = selectedAnakId,
                selectedMakananId = selectedMakananId,
                porsi = porsi,
                onMakananSelected = { id -> selectedMakananId = id },
                onAnakSelected = { id -> selectedAnakId = id },
                onPorsiChanged = { newPorsi -> porsi = newPorsi },
                onSubmit = {
                    val anakId = selectedAnakId
                    val makananId = selectedMakananId
                    val porsiInt = porsi.toIntOrNull() ?: 0
                    if (anakId != null && makananId != null) {
                        makananAnakViewModel.storeMakananAnak(anakId, makananId, porsiInt)
                    } else {
                        makananAnakViewModel.showToastMessage("Harap pilih anak dan makanan")
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
fun MainContent(
    makananList: List<Makanan>,
    anakList: List<Anak>,
    selectedAnakId: Long?,
    selectedMakananId: Long?,
    porsi: String,
    onMakananSelected: (Long) -> Unit,
    onAnakSelected: (Long) -> Unit,
    onPorsiChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        // Menampilkan daftar makanan
//        Text(text = "Daftar Makanan", style = MaterialTheme.typography.h6)
        Text(text = "Daftar Makanan", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.fillMaxHeight(0.3f)) {
            items(makananList) { makanan ->
                Text(
                    text = "Makanan: ${makanan.namaMakanan}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedMakananId?.let { onMakananSelected(it) } }
                )
            }
        }

        // Menampilkan daftar anak
        Text(text = "Daftar Anak", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
        LazyColumn(modifier = Modifier.fillMaxHeight(0.3f)) {
            items(anakList) { anak ->
                Text(
                    text = "Anak: ${anak.namaAnak}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedAnakId?.let { onAnakSelected(it) } }
                )
            }
        }

        // Form input untuk porsi
        Text(text = "Masukkan Porsi", modifier = Modifier.padding(top = 16.dp))
        TextField(
            value = porsi,
            onValueChange = { onPorsiChanged(it) },
            label = { Text("Porsi") },
            modifier = Modifier.fillMaxWidth()
        )

        // Tombol submit
        Button(
            onClick = { onSubmit() },
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
        ) {
            Text("Kirim Makanan yang Dikonsumsi Anak")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen()
}
