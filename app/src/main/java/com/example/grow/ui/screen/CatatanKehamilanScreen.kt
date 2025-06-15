package com.example.grow.ui.screen

// Compose & UI
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// ViewModel & Coroutine
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

// Date & Time
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Logging (optional)
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Data model
import com.example.grow.data.model.CatatanKehamilan
import com.example.grow.ui.theme.BiruMudaMain
import com.example.grow.ui.theme.PoppinsFamily
import com.example.grow.ui.theme.TextPrimary
import com.example.grow.viewmodel.CatatanKehamilanViewModel
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatatanKehamilanScreen(
    viewModel: CatatanKehamilanViewModel = hiltViewModel(),
    navController: NavController,
    userId: Int
) {
    val context = LocalContext.current
    val kehamilan = viewModel.kehamilan
    val catatanList = viewModel.catatanList
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDateConflictDialog by remember { mutableStateOf(false) }

    var beratInput by remember { mutableStateOf("") }
    var tanggalInput by remember { mutableStateOf(LocalDate.now().toString()) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.now().toEpochDay() * 24 * 60 * 60 * 1000
    )
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    var selectedItem by remember { mutableStateOf<CatatanKehamilan?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.fetchKehamilan(userId)
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kehamilan",
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
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            kehamilan?.let { data ->
                val usiaKehamilan = ChronoUnit.WEEKS.between(
                    LocalDate.parse(data.tanggal_mulai),
                    LocalDate.now()
                )

                // Card untuk Status Kandungan
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Status Kandungan: ${data.status}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showStatusDialog = true },
                            colors = ButtonDefaults.buttonColors(BiruMudaMain)
                        ) {
                            Text("Ubah Status")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card untuk Info Kehamilan
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Usia Kehamilan: $usiaKehamilan minggu", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tanggal Awal Kehamilan: ${data.tanggal_mulai}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Berat Awal: ${data.berat_awal} kg", style = MaterialTheme.typography.bodyLarge)
                    }
                }

            } ?: Text("Memuat data kehamilan...")

            Spacer(modifier = Modifier.height(24.dp))

            // Card untuk Riwayat Berat Badan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Berat Badan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { showDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Catatan"
                            )
                        }
                    }

                    // Teks petunjuk
                    Text(
                        text = "Gulir ke bawah untuk melihat lebih banyak",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (catatanList.isNotEmpty()) {
                        val scrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .verticalScroll(scrollState)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tanggal",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.35f)
                                )
                                Text(
                                    text = "Berat (kg)",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.3f)
                                )
                                Text(
                                    text = "Aksi",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.35f)
                                )
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.4f))

                            // Isi data
                            catatanList.sortedByDescending { LocalDate.parse(it.tanggal) }.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.tanggal,
                                        modifier = Modifier.weight(0.35f)
                                    )
                                    Text(
                                        text = "${item.berat}",
                                        modifier = Modifier.weight(0.3f)
                                    )
                                    Row(
                                        modifier = Modifier.weight(0.35f),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(onClick = {
                                            selectedItem = item
                                            beratInput = item.berat.toString()
                                            tanggalInput = item.tanggal
                                            showDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                                        }
                                        IconButton(onClick = {
                                            selectedItem = item
                                            showDeleteConfirmDialog = true
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus")
                                        }
                                    }
                                }
                                Divider(color = Color.LightGray.copy(alpha = 0.2f))
                            }
                        }
                    } else {
                        Text("Belum ada catatan berat badan.")
                    }
                }
            }
        }

        if (showStatusDialog) {
            AlertDialog(
                onDismissRequest = { showStatusDialog = false },
                title = { Text("Ubah Status Kehamilan") },
                text = {
                    Column {
                        val opsi = listOf("Mengandung", "Sudah Melahirkan", "Keguguran")
                        opsi.forEach { statusBaru ->
                            Text(
                                text = statusBaru,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            kehamilan?.let {
                                                viewModel.updateStatusKehamilan(
                                                    id = it.id_kehamilan,
                                                    status = statusBaru
                                                )
                                            }
                                            showStatusDialog = false
                                        }
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showStatusDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        // Dialog Tambah Catatan
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    selectedItem = null
                },
                title = {
                    Text(if (selectedItem == null) "Tambah Catatan Kehamilan" else "Edit Catatan Kehamilan")
                },
                text = {
                    Column {
                        if (selectedItem == null) {
                            // Tambah: input tanggal bisa diedit
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true }
                                    .padding(vertical = 8.dp)
                                    .height(64.dp)
                                    .border(
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = tanggalInput,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            // Edit: tanggal hanya ditampilkan
                            Text("Tanggal: ${selectedItem?.tanggal}")
                        }

                        OutlinedTextField(
                            value = beratInput,
                            onValueChange = { beratInput = it },
                            label = { Text("Berat Badan (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val beratFinal = beratInput.toFloatOrNull() ?: 0f

                        if (selectedItem != null) {
                            // Mode Edit
                            kehamilan?.let {
                                scope.launch {
                                    viewModel.updateCatatan(
                                        userId = userId,
                                        catatan = selectedItem!!.copy(berat = beratFinal)
                                    )
                                    showDialog = false
                                    selectedItem = null
                                }
                            }
                        } else {
                            // Mode Tambah
                            val duplikat = catatanList.any { it.tanggal == tanggalInput }
                            if (duplikat) {
                                showDateConflictDialog = true
                            } else {
                                kehamilan?.let {
                                    scope.launch {
                                        viewModel.simpanCatatan(
                                            userId = userId,
                                            catatan = CatatanKehamilan(
                                                id_kehamilan = it.id_kehamilan,
                                                berat = beratFinal,
                                                tanggal = tanggalInput
                                            )
                                        )
                                        showDialog = false
                                        beratInput = ""
                                        tanggalInput = LocalDate.now().toString()
                                    }
                                }
                            }
                        }
                    }) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        selectedItem = null
                    }) {
                        Text("Batal")
                    }
                }
            )
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Hapus Catatan") },
                text = { Text("Yakin ingin menghapus catatan tanggal ${selectedItem?.tanggal}?") },
                confirmButton = {
                    TextButton(onClick = {
                        selectedItem?.let { item ->
                            scope.launch {
                                viewModel.hapusCatatan(
                                    userId = userId,
                                    catatan = item
                                )
                                showDeleteConfirmDialog = false
                                selectedItem = null
                            }
                        }
                    }) {
                        Text("Hapus", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteConfirmDialog = false
                        selectedItem = null
                    }) {
                        Text("Batal")
                    }
                }
            )
        }

        // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate =
                                LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            tanggalInput = selectedDate.format(formatter)
                        }
                        showDatePicker = false
                    }) {
                        Text("Pilih")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Batal")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Alert jika tanggal sudah ada
        if (showDateConflictDialog) {
            AlertDialog(
                onDismissRequest = { showDateConflictDialog = false },
                title = { Text("Tanggal Sudah Ada") },
                text = { Text("Catatan berat badan untuk tanggal ini sudah tersedia.") },
                confirmButton = {
                    TextButton(onClick = { showDateConflictDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
