package com.example.grow.ui.screen

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import androidx.compose.ui.text.style.TextAlign
import java.util.Locale
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.grow.model.MakananIbu
import com.example.grow.model.StandarNutrisi
import com.example.grow.ui.components.rememberDatePickerDialog
import com.example.grow.viewmodel.AsupanViewModel
import com.example.grow.viewmodel.KehamilanViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
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

    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf("") }
    val datePickerDialog = rememberDatePickerDialog { date -> selectedDate = date }

    LaunchedEffect(userId, tanggalHariIni) {
        viewModelAsupan.checkAsupanHariIni(userId, tanggalHariIni)
        viewModelKehamilan.loadUsiaKehamilan(userId)
    }

    LaunchedEffect(sudahAdaAsupan, usiaKehamilan) {
        if (sudahAdaAsupan == true && usiaKehamilan != null) {
            viewModelAsupan.fetchMakananIbu(userId)

            val kategori = when (usiaKehamilan!!.bulan) {
                in 0..3 -> "kehamilan_0_3_bulan"
                in 4..6 -> "kehamilan_4_6_bulan"
                else -> "kehamilan_7_9_bulan"
            }

            viewModelAsupan.fetchStandarNutrisi(kategori)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analisis Asupan Harian") }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 👶 Usia kehamilan
            usiaKehamilan?.let { usia ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = "Usia kehamilan: ${usia.bulan} bulan ${usia.hari} hari",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 🧪 Judul dan grafik hasil
            Text(
                text = "Hasil Analisis",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (sudahAdaAsupan) {
                true -> {
                    if (makananList.isEmpty() || standarList.isEmpty()) {
                        Text("Belum ada data makanan atau standar nutrisi.")
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(370.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                        ) {
                            GrafikHasil(
                                makananList = makananList,
                                standarList = standarList
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val totalKarbo = makananList.sumOf { it.hasil_analisis.karbohidrat.toDouble() }.toFloat()
                        val totalProtein = makananList.sumOf { it.hasil_analisis.protein.toDouble() }.toFloat()

                        val standarKarbo = standarList.find { it.id_nutrisi == 1 }?.nilai_min ?: 0f
                        val standarProtein = standarList.find { it.id_nutrisi == 2 }?.nilai_min ?: 0f

                        val isCukup = totalKarbo >= standarKarbo && totalProtein >= standarProtein

                        val pesan = if (isCukup) {
                            "Asupan Moms hari ini sudah cukup. Pertahankan pola makan seperti ini di hari-hari berikutnya demi kesehatan si kecil!"
                        } else {
                            "Asupan Moms hari ini masih kurang dari standar. Yuk, tambah porsi makan bergizi agar kebutuhan nutrisi si kecil terpenuhi dengan baik!"
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isCukup) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Text(
                                text = pesan,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isCukup) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 💡 Placeholder Rekomendasi
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Rekomendasi Makanan Buat Moms Biar Ga Bosen",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada rekomendasi makanan. Fitur ini akan segera hadir.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                false -> {
                    Text("Belum ada data asupan untuk hari ini")
                    Spacer(modifier = Modifier.height(12.dp))

                    if (usiaKehamilan == null) {
                        // Jika belum ada data kehamilan, tampilkan dua tombol:
                        Button(
                            onClick = { navController.navigate("kehamilan") },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = "Tambah Data Kehamilan")
                        }

                        Button(
                            onClick = {
                                // Jika klik pilih tanggal konsumsi tapi data kehamilan belum ada,
                                // munculkan notifikasi dulu
                                Toast.makeText(context, "Silakan input data kehamilan terlebih dahulu", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = "Pilih Tanggal Konsumsi")
                        }
                    } else {
                        // Jika sudah ada data kehamilan, tampilkan tombol pilih tanggal konsumsi seperti biasa
                        if (selectedDate.isNotEmpty()) {
                            Button(
                                onClick = {
                                    navController.navigate("asupan_screen/$userId/$selectedDate")
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(text = "Tambah Data Asupan untuk $selectedDate")
                            }
                        } else {
                            Button(
                                onClick = { datePickerDialog.show() },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(text = "Pilih Tanggal Konsumsi")
                            }
                        }
                    }
                }

                null -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

//            if (usiaKehamilan == null) {
//                Button(
//                    onClick = { navController.navigate("kehamilan") },
//                    shape = RoundedCornerShape(50),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp)
//                ) {
//                    Text(text = "Tambah Data Kehamilan")
//                }
//            }
        }
    }
}

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
                    setBackgroundColor(android.graphics.Color.WHITE)
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
                .height(320.dp)
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