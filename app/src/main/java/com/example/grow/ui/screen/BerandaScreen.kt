package com.example.grow.ui.screen

import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.text.input.KeyboardType
import com.example.grow.model.AnalisisData
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BerandaScreen(
    userId: Int,
    idAnak: Int,
    navController: NavController,
    viewModelAsupan: AsupanViewModel = hiltViewModel(),
    viewModelKehamilan: KehamilanViewModel = hiltViewModel()
) {
    //ibu
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val tanggalFromArg = navBackStackEntry?.arguments?.getString("tanggal")
    val tanggalHariIni = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date())
    }
    var selectedDate by rememberSaveable {
        mutableStateOf(tanggalFromArg ?: tanggalHariIni)
    }

    val sudahAdaAsupan by viewModelAsupan.asupanHariIni.collectAsState()
    val usiaKehamilan by viewModelKehamilan.usiaKehamilan.collectAsState()
    val makananList by viewModelAsupan.makananIbuData
    val standarList by viewModelAsupan.standarNutrisi

    val context = LocalContext.current
    val datePickerDialog = rememberDatePickerDialog {
        date -> selectedDate = date
        viewModelAsupan.setTanggalDipilih(date)
    }
    var pilihanInput by rememberSaveable { mutableStateOf("kehamilan") }

    //anak
    val tanggalKonsumsi by viewModelAsupan.tanggalKonsumsi.collectAsState()
    val jumlahPorsi by viewModelAsupan.jumlahPorsi.collectAsState()
    val isLoading by viewModelAsupan.loading.collectAsState()
    val error by viewModelAsupan.error.collectAsState()
    val dataAnalisis by viewModelAsupan.dataAnalisis.collectAsState()
    val dataSudahAda by viewModelAsupan.dataSudahAda.collectAsState()
    val usiaAnak by viewModelAsupan.usiaAnak

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModelAsupan.initTanggalHariIniJikaKosong()
    }

    LaunchedEffect(tanggalKonsumsi) {
        if (tanggalKonsumsi.isNotBlank()) {
            viewModelAsupan.getAsupanAnakByIdAnakAndTanggal(idAnak, tanggalKonsumsi)
        }
    }

    LaunchedEffect(userId, selectedDate) {
        viewModelAsupan.setTanggalDipilih(selectedDate)
        viewModelAsupan.checkAsupanHariIni(userId, selectedDate)
        viewModelKehamilan.loadUsiaKehamilan(userId)
        viewModelAsupan.fetchMakananIbu(userId, selectedDate)
    }

    LaunchedEffect(sudahAdaAsupan, usiaKehamilan) {
        if (sudahAdaAsupan == true && usiaKehamilan != null) {

            val kategori = when (usiaKehamilan!!.bulan) {
                in 0..3 -> "kehamilan_0_3_bulan"
                in 4..6 -> "kehamilan_4_6_bulan"
                else -> "kehamilan_7_9_bulan"
            }

            viewModelAsupan.fetchStandarNutrisi(kategori)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Judul
        Text("Pilih Jenis Analisis yang Ingin Dilakukan", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        // Opsi input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Opsi Ibu Hamil
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = pilihanInput == "kehamilan",
                    onClick = { pilihanInput = "kehamilan" }
                )
                Text("Analisis Asupan Ibu Hamil")
            }

            // Opsi Anak
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = pilihanInput == "asupan_anak",
                    onClick = { pilihanInput = "asupan_anak" }
                )
                Text("Analisis Asupan Asi Anak")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (pilihanInput) {
            "kehamilan" -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Analisis Asupan Ibu Hamil") }
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
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ganti Tanggal Konsumsi (Saat Ini: $selectedDate)")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

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
                                }
                            }

                            null -> {
                                CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            "asupan_anak" -> {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Analisis Asupan Asi Anak") })
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()) // <-- scrollable
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Analisis Frekuensi Anak Menyusui", style = MaterialTheme.typography.headlineSmall)

                        // === Informasi Usia Anak ===
                        if (usiaAnak.isNotBlank()) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Usia Anak:", style = MaterialTheme.typography.titleSmall)
                                    Text(usiaAnak, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                        }

                        // === Pilih Tanggal ===
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                            }
                        }

                        if (showDatePicker) {
                            rememberDatePickerDialog(
                                onDateSelected = {
                                    viewModelAsupan.setTanggalKonsumsi(it)
                                    showDatePicker = false
                                }
                            ).show()
                        }

                        // === Error Message ===
                        error?.let {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = it,
                                    color = Color.Red,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        // === Bagian Hasil atau Form Input ===
                        if (tanggalKonsumsi.isNotBlank()) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                if (dataSudahAda) {
                                    // === Grafik Analisis ===
                                    dataAnalisis?.let { analisis ->
                                        Card(modifier = Modifier.fillMaxWidth()) {
                                            AsiBarChart(data = analisis)
                                        }
                                    }

                                    // === Pesan Analisis ===
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
                                    // === Form Input Frekuensi ===
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            OutlinedTextField(
                                                value = jumlahPorsi,
                                                onValueChange = { viewModelAsupan.setJumlahPorsi(it) },
                                                label = { Text("Jumlah Frekuensi Anak Menyusui Hari Ini") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Button(
                                                onClick = { viewModelAsupan.inputAsupan(idAnak) },
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
                    }
                }
            }
        }
    }
}
//ibu
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
                    description.isEnabled = false
                    axisRight.isEnabled = false
                }
            },
            update = { chart ->
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

                val konsumsiDataSet = BarDataSet(konsumsiEntries, "Konsumsi").apply { color = android.graphics.Color.BLUE }
                val standarDataSet = BarDataSet(standarEntries, "Standar").apply { color = android.graphics.Color.RED }

                val barData = BarData(konsumsiDataSet, standarDataSet)
                barData.barWidth = 0.4f
                chart.data = barData

                chart.xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(labels)
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setCenterAxisLabels(true)
                    axisMinimum = 0f
                    axisMaximum = 0f + barData.getGroupWidth(0.2f, 0f) * labels.size
                }

                chart.axisLeft.axisMinimum = 0f

                barData.groupBars(0f, 0.2f, 0f)
                chart.invalidate()
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

//anak
fun getKategoriUsiaDanStandar(usiaAnak: String): Pair<String, Int> {
    val regex = Regex("""(\d+)\s*bulan\s*(\d+)?\s*hari?""")
    val match = regex.find(usiaAnak.lowercase())

    val bulan = match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0

    return when {
        bulan < 1 -> "0 - 1 bulan" to 8
        bulan < 2 -> "1 - 2 bulan" to 8
        bulan < 4 -> "2 - 4 bulan" to 8
        bulan in 4..24 -> "4 - 24 bulan" to 3
        else -> "di atas 24 bulan" to 3 // fallback
    }
}

fun generatePesanAnalisis(
    usiaAnak: String,
    standar: Int,
    jumlahPorsi: Int
): String {
    val kategoriUsia = getKategoriUsiaDanStandar(usiaAnak).first
    return if (jumlahPorsi >= standar) {
        "Anak kamu sekarang berusia sekitar $kategoriUsia, yang dimana harus diberi ASI setidaknya $standar kali sehari, dan dari data, anak kamu sudah memenuhinya. Jangan lupa untuk diberikan ASI dengan tingkat pemberian yang sama di hari berikutnya!"
    } else {
        "Anak kamu sekarang berusia sekitar $kategoriUsia, yang dimana harus diberi ASI setidaknya $standar kali sehari, dan dari data, dapat dilihat bahwa anak kamu belum memenuhi pemberian ASI-nya. Jangan lupa untuk diberikan ASI sesuai dengan standar pemberian ASI pada hari berikutnya!"
    }
}


@Composable
fun AsiBarChart(data: AnalisisData) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            BarChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                val entries = listOf(
                    BarEntry(0f, data.jumlahPorsiDikonsumsi.toFloat()), // Konsumsi aktual
                    BarEntry(1f, data.standarFrekuensi.toFloat())       // Standar
                )

                val dataSet = BarDataSet(entries, "Jumlah Porsi ASI").apply {
                    colors = listOf(ColorTemplate.COLORFUL_COLORS[0], ColorTemplate.COLORFUL_COLORS[1])
                    valueTextSize = 12f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String = value.toInt().toString()
                    }
                }

                val barData = BarData(dataSet)
                this.data = barData

                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(listOf("Anak Menyusui", "Standar Anak Menyusui"))
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                }

                axisLeft.axisMinimum = 0f
                axisRight.isEnabled = false

                description.isEnabled = false
                legend.isEnabled = false

                animateY(1000)
                invalidate()
            }
        }
    )
}