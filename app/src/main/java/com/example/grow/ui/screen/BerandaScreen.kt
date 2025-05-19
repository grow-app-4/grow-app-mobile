package com.example.grow.ui.screen

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.grow.model.MakananIbu
import com.example.grow.model.StandarNutrisi
import com.example.grow.ui.components.rememberDatePickerDialog
import com.example.grow.viewmodel.AsupanViewModel
import com.example.grow.viewmodel.KehamilanViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BerandaScreen(
    userId: Int,
    navController: NavController,
    viewModelAsupan: AsupanViewModel = hiltViewModel(),
    viewModelKehamilan: KehamilanViewModel = hiltViewModel()
) {
    val tanggalHariIni = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date())
    }

    val sudahAdaAsupan by viewModelAsupan.asupanHariIni.collectAsState()
    val usiaKehamilan by viewModelKehamilan.usiaKehamilan.collectAsState()

    val makananList by viewModelAsupan.makananIbuData
    val standarList by viewModelAsupan.standarNutrisi

//    var selectedDate by remember { mutableStateOf(tanggalHariIni) } // tanggal otomatis hari ini
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf("") }
    val datePickerDialog = rememberDatePickerDialog { date ->
        selectedDate = date
    }

    LaunchedEffect(userId, tanggalHariIni) {
        viewModelAsupan.checkAsupanHariIni(userId, tanggalHariIni)
        viewModelKehamilan.loadUsiaKehamilan(userId)
    }

    LaunchedEffect(sudahAdaAsupan) {
        if (sudahAdaAsupan == true) {
            viewModelAsupan.fetchMakananIbu(userId)
            viewModelAsupan.fetchStandarNutrisi("kehamilan_0_3_bulan")
        }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Beranda", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        if (usiaKehamilan == null) {
            Text("Data kehamilan belum tersedia.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navController.navigate("kehamilan") }) {
                Text("Tambah Data Kehamilan")
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            usiaKehamilan?.let { usia ->
                Text(
                    text = "Usia kehamilan: ${usia.bulan} bulan ${usia.hari} hari",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        when (sudahAdaAsupan) {
            true -> {
                if (makananList.isEmpty() || standarList.isEmpty()) {
                    Text("Belum ada data makanan atau standar nutrisi.")
                } else {
                    // Grafik langsung di sini (dari kode GrafikHasilScreen)
                    GrafikHasil(
                        makananList = makananList,
                        standarList = standarList
                    )
                }
            }

            false -> {
                Text("Belum ada data asupan untuk hari ini")
                Spacer(modifier = Modifier.height(8.dp))

                if (selectedDate.isNotEmpty()) {
                    Button(onClick = {
                        navController.navigate("asupan_screen/$userId/$selectedDate")
                    }) {
                        Text("Tambah Data Asupan untuk $selectedDate")
                    }
                } else {
                    Button(onClick = { datePickerDialog.show() }) {
                        Text("Pilih Tanggal Konsumsi")
                    }
                }
            }

            null -> {
                CircularProgressIndicator()
            }
        }
    }
}

// Pisahkan grafik ke fungsi @Composable untuk rapi
@Composable
fun GrafikHasil(
    makananList: List<MakananIbu>,
    standarList: List<StandarNutrisi>
) {
    val idToNamaNutrisiMap = mapOf(
        1 to "karbohidrat",
        2 to "protein"
    )

    val totalNutrisi = remember(makananList) {
        val map = mutableMapOf<String, Float>()
        makananList.forEach { item ->
            val analisis = item.hasil_analisis
            map["karbohidrat"] = map.getOrDefault("karbohidrat", 0f) + analisis.karbohidrat
            map["protein"] = map.getOrDefault("protein", 0f) + analisis.protein
        }
        map
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Perbandingan Nutrisi vs Standar", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        AndroidView(
            factory = { context ->
                BarChart(context).apply {
                    val labels = mutableListOf<String>()
                    val konsumsiEntries = mutableListOf<BarEntry>()
                    val standarEntries = mutableListOf<BarEntry>()

                    var entryIndex = 0
                    standarList.forEach { standar ->
                        val namaNutrisiKey = idToNamaNutrisiMap[standar.id_nutrisi]
                        if (namaNutrisiKey != null) {
                            val nilaiKonsumsi = totalNutrisi[namaNutrisiKey] ?: 0f
                            labels.add(namaNutrisiKey)
                            konsumsiEntries.add(BarEntry(entryIndex.toFloat(), nilaiKonsumsi))
                            standarEntries.add(BarEntry(entryIndex.toFloat(), standar.nilai_min))
                            entryIndex++
                        }
                    }

                    val konsumsiDataSet = BarDataSet(konsumsiEntries, "Konsumsi").apply { color = Color.BLUE }
                    val standarDataSet = BarDataSet(standarEntries, "Standar").apply { color = Color.RED }

                    val barData = BarData(konsumsiDataSet, standarDataSet)
                    barData.barWidth = 0.4f
                    data = barData

                    xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setCenterAxisLabels(true)
                    xAxis.axisMinimum = 0f

                    val groupCount = labels.size
                    val groupSpace = 0.2f
                    val barSpace = 0f
                    val groupWidth = barData.getGroupWidth(groupSpace, barSpace)

                    xAxis.axisMaximum = 0f + groupWidth * groupCount
                    barData.groupBars(0f, groupSpace, barSpace)

                    axisLeft.axisMinimum = 0f
                    axisRight.isEnabled = false
                    description.isEnabled = false

                    invalidate()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Detail Perbandingan:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        standarList.forEach { standar ->
            val namaNutrisiKey = idToNamaNutrisiMap[standar.id_nutrisi]
            if (namaNutrisiKey != null) {
                val konsumsi = totalNutrisi[namaNutrisiKey] ?: 0f
                val status = if (konsumsi >= standar.nilai_min) "✅ Cukup" else "❌ Kurang"
                Text("- Nutrisi $namaNutrisiKey: ${"%.1f".format(konsumsi)} vs ${"%.1f".format(standar.nilai_min)} → $status")
            }
        }
    }
}
