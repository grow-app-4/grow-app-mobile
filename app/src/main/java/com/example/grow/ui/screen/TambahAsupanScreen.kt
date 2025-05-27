package com.example.grow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.grow.data.model.Makanan
import com.example.grow.viewmodel.AsupanViewModel
import com.example.grow.enu.KategoriMakanan
import com.example.grow.ui.theme.*

data class FoodEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val selectedMakananId: Int? = null,
    val jumlahPorsi: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahAsupanScreen(
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

    // State untuk menyimpan entries per kategori
    var foodEntries by remember { mutableStateOf(listOf(FoodEntry())) }

    LaunchedEffect(Unit) {
        viewModel.loadMakananIbuHamil()
    }

    LaunchedEffect(currentStepIndex) {
        // Reset entries ketika ganti kategori
        foodEntries = listOf(FoodEntry())
    }

    // Tampilkan snackbar saat result pertama kali muncul
    LaunchedEffect(result) {
        result?.let {
            snackbarHostState.showSnackbar("Analisis berhasil! Silakan lihat grafik hasil.")
            showNavigateButton = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tambah Data Asupan",
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Kategori Header
            Text(
                text = "Kategori ${currentKategori.label}",
                modifier = Modifier.padding(bottom = 16.dp),
                style = TextStyle(
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(foodEntries) { index, entry ->
                    FoodEntryCard(
                        entry = entry,
                        listMakanan = listMakanan,
                        onEntryChange = { newEntry ->
                            foodEntries = foodEntries.toMutableList().apply {
                                set(index, newEntry)
                            }
                            // Update ViewModel
                            newEntry.selectedMakananId?.let { makananId ->
                                viewModel.pilihMakanan(makananId, newEntry.jumlahPorsi)
                            }
                        },
                        onDelete = if (foodEntries.size > 1) {
                            {
                                foodEntries = foodEntries.toMutableList().apply {
                                    removeAt(index)
                                }
                            }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol untuk menambah entry baru
            Button(
                onClick = {
                    foodEntries = foodEntries + FoodEntry()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Tambah Data ${currentKategori.label}",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStepIndex > 0) {
                    Button(
                        onClick = { currentStepIndex-- },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Kembali",
                            style = TextStyle(
                                fontFamily = PoppinsFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (currentStepIndex < kategoriList.lastIndex) {
                    Button(
                        onClick = { currentStepIndex++ },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Lanjut",
                            style = TextStyle(
                                fontFamily = PoppinsFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.kirimAnalisis(idUser, tanggalKonsumsi) },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Simpan",
                            style = TextStyle(
                                fontFamily = PoppinsFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    color = Blue
                )
            }

            result?.let {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightBlue)
                        .padding(16.dp)
                ) {
                    Text(
                        "✅ Analisis berhasil dilakukan.",
                        style = TextStyle(
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Blue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        navController.navigate(Screen.Nutrisi.route) {
                            popUpTo(Screen.TambahAsupan.createRoute(idUser, tanggalKonsumsi)) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Lihat Grafik Hasil",
                        style = TextStyle(
                            fontFamily = PoppinsFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodEntryCard(
    entry: FoodEntry,
    listMakanan: List<Makanan>,
    onEntryChange: (FoodEntry) -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var porsiText by remember(entry.jumlahPorsi) { mutableStateOf(entry.jumlahPorsi.toString()) }
    val selectedMakanan = listMakanan.find { it.id_makanan == entry.selectedMakananId }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedMakanan?.nama_makanan ?: "Pilih Makanan",
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                )
                onDelete?.let {
                    IconButton(onClick = it, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pilih Makanan",
                modifier = Modifier.padding(bottom = 8.dp),
                style = TextStyle(
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                TextField(
                    value = selectedMakanan?.nama_makanan ?: "Pilih makanan...",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Blue
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LightBlue,
                        unfocusedContainerColor = LightBlue,
                        disabledContainerColor = LightBlue,
                        focusedIndicatorColor = Blue,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontSize = 14.sp,
                        color = if (selectedMakanan != null) TextPrimary else TextSecondary
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listMakanan.forEach { makanan ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    makanan.nama_makanan,
                                    style = TextStyle(
                                        fontFamily = PoppinsFamily,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                )
                            },
                            onClick = {
                                onEntryChange(entry.copy(selectedMakananId = makanan.id_makanan))
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (selectedMakanan != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ukuran / Porsi",
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = TextStyle(
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = porsiText,
                    onValueChange = { newText ->
                        porsiText = newText
                        val jumlah = newText.toIntOrNull() ?: 1
                        onEntryChange(entry.copy(jumlahPorsi = jumlah))
                    },
                    placeholder = {
                        Text(
                            "Contoh: 1",
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
    }
}