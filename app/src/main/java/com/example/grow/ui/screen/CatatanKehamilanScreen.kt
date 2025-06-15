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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalContext

// Data model
import com.example.grow.data.model.CatatanKehamilan
import com.example.grow.viewmodel.CatatanKehamilanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatatanKehamilanScreen(
    viewModel: CatatanKehamilanViewModel = hiltViewModel(),
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

    LaunchedEffect(userId) {
        viewModel.fetchKehamilan(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Catatan Kehamilan Ibu",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        kehamilan?.let { data ->
            val usiaKehamilan = ChronoUnit.WEEKS.between(
                LocalDate.parse(data.tanggal_mulai),
                LocalDate.now()
            )
            Text("Status Kandungan: ${data.status}")
            Text("Usia Kehamilan: $usiaKehamilan minggu")
            Text("Tanggal Awal Kehamilan: ${data.tanggal_mulai}")
            Text("Berat Awal: ${data.berat_awal} kg")
        } ?: Text("Memuat data kehamilan...")

        Spacer(modifier = Modifier.height(24.dp))

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

        Spacer(modifier = Modifier.height(8.dp))

        if (catatanList.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tanggal", fontWeight = FontWeight.Bold)
                        Text("Berat (kg)", fontWeight = FontWeight.Bold)
                    }
                }

                items(catatanList) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.tanggal)
                        Text("${item.berat}")
                    }
                }
            }
        } else {
            Text("Belum ada catatan berat badan.")
        }
    }

    // Dialog Tambah Catatan
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Tambah Catatan Kehamilan") },
            text = {
                Column {
                    OutlinedTextField(
                        value = beratInput,
                        onValueChange = { beratInput = it },
                        label = { Text("Berat Badan (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
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
                }
            },
            confirmButton = {
                TextButton(onClick = {
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
                                        berat = beratInput.toFloatOrNull() ?: 0f,
                                        tanggal = tanggalInput
                                    )
                                )
                                beratInput = ""
                                tanggalInput = LocalDate.now().toString()
                                showDialog = false
                            }
                        }
                    }
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
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
