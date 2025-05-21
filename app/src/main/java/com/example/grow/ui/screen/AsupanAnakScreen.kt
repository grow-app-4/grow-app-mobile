package com.example.grow.ui.screen

import android.util.Log
import android.view.ViewGroup
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.grow.model.AnalisisData
import com.example.grow.ui.components.rememberDatePickerDialog
import com.example.grow.viewmodel.AsupanViewModel // Ganti sesuai paketmu
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun AsupanAnakScreen(
    idAnak: Int,
    viewModel: AsupanViewModel = hiltViewModel()
) {
    val tanggalKonsumsi by viewModel.tanggalKonsumsi.collectAsState()
    val jumlahPorsi by viewModel.jumlahPorsi.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val dataAnalisis by viewModel.dataAnalisis.collectAsState()
    val dataSudahAda by viewModel.dataSudahAda.collectAsState()
    val usiaAnak by viewModel.usiaAnak

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initTanggalHariIniJikaKosong()
    }

    LaunchedEffect(tanggalKonsumsi) {
        if (tanggalKonsumsi.isNotBlank()) {
            viewModel.getAsupanAnakByIdAnakAndTanggal(idAnak, tanggalKonsumsi)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Analisis Frekuensi Anak Menyusui", style = MaterialTheme.typography.headlineSmall)

        if (usiaAnak.isNotBlank()) {
            Text(
                text = "Usia Anak: $usiaAnak",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        OutlinedTextField(
            value = tanggalKonsumsi,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tanggal Anak Menyusui") },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Pilih tanggal")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (showDatePicker) {
            rememberDatePickerDialog(
                onDateSelected = {
                    viewModel.setTanggalKonsumsi(it)
                    showDatePicker = false
                }
            ).show()
        }

        error?.let {
            Text(text = it, color = Color.Red)
        }

        if (tanggalKonsumsi.isNotBlank()) {
            Log.d("AsupanAnakScreen", "tanggal: $tanggalKonsumsi, dataSudahAda: $dataSudahAda, isLoading: $isLoading")
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                if (dataSudahAda) {
                    dataAnalisis?.let { analisis ->
                        AsiBarChart(data = analisis)
                    }
                    dataAnalisis?.let { analisis ->
                        if (usiaAnak.isNotBlank()) {
                            val pesan = generatePesanAnalisis(
                                usiaAnak = usiaAnak,
                                standar = analisis.standarFrekuensi,
                                jumlahPorsi = analisis.jumlahPorsiDikonsumsi
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Hasil Analisis:",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(pesan, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                } else {
                    // Jika data belum ada, tampilkan form input
                    OutlinedTextField(
                        value = jumlahPorsi,
                        onValueChange = { viewModel.setJumlahPorsi(it) },
                        label = { Text("Jumlah Frekuensi Anak Menyusui Hari Ini") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.inputAsupan(idAnak) },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Kirim Data Asupan Menyusui")
                        }
                    }
                }
            }
        }
    }
}

//fun getKategoriUsiaDanStandar(usiaAnak: String): Pair<String, Int> {
//    val regex = Regex("""(\d+)\s*bulan\s*(\d+)?\s*hari?""")
//    val match = regex.find(usiaAnak.lowercase())
//
//    val bulan = match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
//
//    return when {
//        bulan < 1 -> "0 - 1 bulan" to 8
//        bulan < 2 -> "1 - 2 bulan" to 8
//        bulan < 4 -> "2 - 4 bulan" to 8
//        bulan in 4..24 -> "4 - 24 bulan" to 3
//        else -> "di atas 24 bulan" to 3 // fallback
//    }
//}
//
//fun generatePesanAnalisis(
//    usiaAnak: String,
//    standar: Int,
//    jumlahPorsi: Int
//): String {
//    val kategoriUsia = getKategoriUsiaDanStandar(usiaAnak).first
//    return if (jumlahPorsi >= standar) {
//        "Anak kamu sekarang berusia sekitar $kategoriUsia, yang dimana harus diberi ASI setidaknya $standar kali sehari, dan dari data, anak kamu sudah memenuhinya. Jangan lupa untuk diberikan ASI dengan tingkat pemberian yang sama di hari berikutnya!"
//    } else {
//        "Anak kamu sekarang berusia sekitar $kategoriUsia, yang dimana harus diberi ASI setidaknya $standar kali sehari, dan dari data, dapat dilihat bahwa anak kamu belum memenuhi pemberian ASI-nya. Jangan lupa untuk diberikan ASI sesuai dengan standar pemberian ASI pada hari berikutnya!"
//    }
//}
//
//
//@Composable
//fun AsiBarChart(data: AnalisisData) {
//    AndroidView(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(300.dp),
//        factory = { context ->
//            BarChart(context).apply {
//                layoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )
//
//                val entries = listOf(
//                    BarEntry(0f, data.jumlahPorsiDikonsumsi.toFloat()), // Konsumsi aktual
//                    BarEntry(1f, data.standarFrekuensi.toFloat())       // Standar
//                )
//
//                val dataSet = BarDataSet(entries, "Jumlah Porsi ASI").apply {
//                    colors = listOf(ColorTemplate.COLORFUL_COLORS[0], ColorTemplate.COLORFUL_COLORS[1])
//                    valueTextSize = 12f
//                    valueFormatter = object : ValueFormatter() {
//                        override fun getFormattedValue(value: Float): String = value.toInt().toString()
//                    }
//                }
//
//                val barData = BarData(dataSet)
//                this.data = barData
//
//                xAxis.apply {
//                    valueFormatter = IndexAxisValueFormatter(listOf("Anak Menyusui", "Standar Anak Menyusui"))
//                    position = XAxis.XAxisPosition.BOTTOM
//                    granularity = 1f
//                    setDrawGridLines(false)
//                }
//
//                axisLeft.axisMinimum = 0f
//                axisRight.isEnabled = false
//
//                description.isEnabled = false
//                legend.isEnabled = false
//
//                animateY(1000)
//                invalidate()
//            }
//        }
//    )
//}
