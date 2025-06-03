package com.example.grow.ui.screen

import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import com.example.grow.data.model.MakananIbu
import com.example.grow.data.model.StandarNutrisi
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
import com.example.grow.data.model.AnalisisData
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.grow.data.AnakEntity

import coil.compose.AsyncImage

// Import colors dari theme
import com.example.grow.ui.theme.BiruPrimer
import com.example.grow.ui.theme.BiruMudaMain
import com.example.grow.ui.theme.BiruMudaSecondary
import com.example.grow.ui.theme.TextColor
import com.example.grow.ui.theme.BackgroundColor
import com.example.grow.ui.theme.BiruText
import com.example.grow.ui.theme.Typography
import com.example.grow.ui.viewmodel.PertumbuhanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutrisiScreen(
    userId: Int,
    navController: NavController,
    viewModelAsupan: AsupanViewModel = hiltViewModel(),
    viewModelKehamilan: KehamilanViewModel = hiltViewModel(),
    viewModelAnak: PertumbuhanViewModel = hiltViewModel()
) {
    // State management
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val tanggalFromArg = navBackStackEntry?.arguments?.getString("tanggal")
    val tanggalHariIni = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date())
    }
    var selectedDate by rememberSaveable {
        mutableStateOf(tanggalFromArg ?: tanggalHariIni)
    }

    val namaPengguna by viewModelKehamilan.namaPengguna.collectAsState()
    val sudahAdaAsupan by viewModelAsupan.asupanHariIni.collectAsState()
    val usiaKehamilan by viewModelKehamilan.usiaKehamilan.collectAsState()
    val makananList by viewModelAsupan.makananIbuData
    val standarList by viewModelAsupan.standarNutrisi

    val context = LocalContext.current
    val datePickerDialog = rememberDatePickerDialog { date ->
        selectedDate = date
        viewModelAsupan.setTanggalDipilih(date)
    }
    var pilihanInput by rememberSaveable { mutableStateOf("kehamilan") }

    val children by viewModelAnak.children.collectAsState()
    val selectedChildIndex by viewModelAnak.selectedChildIndex.collectAsState()
    val selectedChild = children.getOrNull(selectedChildIndex)
    val tanggalKonsumsi by viewModelAsupan.tanggalKonsumsi.collectAsState()
    val jumlahPorsi by viewModelAsupan.jumlahPorsi.collectAsState()
    val isLoading by viewModelAsupan.loading.collectAsState()
    val error by viewModelAsupan.error.collectAsState()
    val dataAnalisis by viewModelAsupan.dataAnalisis.collectAsState()
    val dataSudahAda by viewModelAsupan.dataSudahAda.collectAsState()
    val usiaAnak by viewModelAsupan.usiaAnak

    //resep rekomendasi
    val isLoadingResep = viewModelAsupan.isLoadingResep
    val errorMessageResep = viewModelAsupan.errorMessageResep
    val resepList = viewModelAsupan.resepList

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModelAnak.loadChildren(userId)
    }

    // LaunchedEffects
    LaunchedEffect(Unit) {
        viewModelAsupan.initTanggalHariIniJikaKosong()
    }

    LaunchedEffect(tanggalKonsumsi, selectedChild?.idAnak) {
        selectedChild?.idAnak?.let { idAnak ->
            viewModelAsupan.getAsupanAnakByIdAnakAndTanggal(idAnak, tanggalKonsumsi)
            viewModelAsupan.fetchResep(idAnak.toString(), tanggalKonsumsi)
        }
    }

    LaunchedEffect(userId, selectedDate) {
        viewModelAsupan.setTanggalDipilih(selectedDate)
        viewModelAsupan.checkAsupanHariIni(userId, selectedDate)
        viewModelKehamilan.loadUsiaKehamilan(userId)
        viewModelKehamilan.loadUserData(userId)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp)
    ) {
        // Add Spacer to push the tab row down
        Spacer(modifier = Modifier.height(32.dp))

        // Tab Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BiruMudaMain, RoundedCornerShape(25.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tab Kehamilan
            Button(
                onClick = { pilihanInput = "kehamilan" },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pilihanInput == "kehamilan") BackgroundColor else Color.Transparent,
                    contentColor = if (pilihanInput == "kehamilan") BiruPrimer else TextColor
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (pilihanInput == "kehamilan") 2.dp else 0.dp
                )
            ) {
                Text(
                    text = "kehamilan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Tab ASI Anak
            Button(
                onClick = { pilihanInput = "asupan_anak" },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pilihanInput == "asupan_anak") BackgroundColor else Color.Transparent,
                    contentColor = if (pilihanInput == "asupan_anak") BiruPrimer else TextColor
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (pilihanInput == "asupan_anak") 2.dp else 0.dp
                )
            ) {
                Text(
                    text = "asi anak",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (pilihanInput) {
            "kehamilan" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Profile Section
                    usiaKehamilan?.let { usia ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BiruMudaMain),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar placeholder
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(BiruPrimer, RoundedCornerShape(24.dp))
                                ) {
                                    Text(
                                        text = "👤",
                                        modifier = Modifier.align(Alignment.Center),
                                        fontSize = 24.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = namaPengguna ?: "Memuat...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BiruText
                                    )
                                    Text(
                                        text = "Minggu ${usia.bulan * 4 + (usia.hari / 7)} - Trimester ${
                                            when (usia.bulan) {
                                                in 0..3 -> 1
                                                in 4..6 -> 2
                                                else -> 3
                                            }
                                        }",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = BiruText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (usiaKehamilan != null && sudahAdaAsupan == false) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
                                        )
                                    )
                                    .clickable {
                                        navController.navigate("asupan_screen/$userId/$selectedDate")
                                    }
                                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(30.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Tambah",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tambah Data Asupan",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                    } ?: run {
                        // No pregnancy data card
                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clickable {
                                    navController.navigate(Screen.TambahKehamilan.createRoute(userId))
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BiruMudaMain),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = BiruText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tambah Data Kehamilan",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BiruText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Chart Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Grafik Nutrisi Harian",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextColor
                                )

                                // Make the date card clickable
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xFFF5F5F5
                                        )
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    modifier = Modifier.clickable {
                                        datePickerDialog.show()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedDate,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextColor
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Select Date",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            when (sudahAdaAsupan) {
                                true -> {
                                    if (makananList.isEmpty() || standarList.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Ayo Isi Data Asupan Dulu Ya!",
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    } else {
                                        GrafikHasil(
                                            makananList = makananList,
                                            standarList = standarList
                                        )
                                    }
                                }

                                false -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Belum ada data Asupan Harian",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                null -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = BiruPrimer)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Analysis Result Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hasil Analisis",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextColor
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (sudahAdaAsupan == true && makananList.isNotEmpty() && standarList.isNotEmpty()) {
                                val totalKarbo =
                                    makananList.sumOf { it.hasil_analisis.karbohidrat.toDouble() }
                                        .toFloat()
                                val totalProtein =
                                    makananList.sumOf { it.hasil_analisis.protein.toDouble() }
                                        .toFloat()

                                val standarKarbo =
                                    standarList.find { it.id_nutrisi == 1 }?.nilai_min ?: 0f
                                val standarProtein =
                                    standarList.find { it.id_nutrisi == 2 }?.nilai_min ?: 0f

                                val isCukup =
                                    totalKarbo >= standarKarbo && totalProtein >= standarProtein

                                val pesan = if (isCukup) {
                                    "Asupan Moms hari ini sudah cukup. Pertahankan pola makan seperti ini di hari-hari berikutnya demi kesehatan si kecil!"
                                } else {
                                    "Asupan Moms hari ini masih kurang dari standar. Yuk, tambah porsi makan bergizi agar kebutuhan nutrisi si kecil terpenuhi dengan baik!"
                                }

                                Text(
                                    text = pesan,
                                    fontSize = 14.sp,
                                    color = TextColor,
                                    lineHeight = 20.sp
                                )
                            } else {
                                Text(
                                    text = "Analisis Akan Muncul Setelah Moms Isi Data Asupan!",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Food Recommendation Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            "asupan_anak" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Child Profile Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clickable {
                                // Navigate to add child data if needed
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        ChildProfileHeader(
                            viewModel = viewModelAnak,
                            children = children,
                            selectedChild = selectedChild,
                            onChildChanged = { selectedIndex ->
                                viewModelAnak.selectChild(selectedIndex)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!dataSudahAda && tanggalKonsumsi.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BackgroundColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Input Data ASI",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextColor
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = jumlahPorsi,
                                    onValueChange = { viewModelAsupan.setJumlahPorsi(it) },
                                    label = { Text("Jumlah Frekuensi Anak Menyusui Hari Ini") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BiruPrimer,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { selectedChild?.idAnak?.let { id ->
                                        viewModelAsupan.inputAsupan(id)
                                    } },
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BiruPrimer),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = BackgroundColor
                                        )
                                    } else {
                                        Text(
                                            text = "Kirim Data Asupan Menyusui",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BackgroundColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Chart Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Grafik Nutrisi Harian",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextColor
                                )

                                // Date picker card
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xFFF5F5F5
                                        )
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    modifier = Modifier.clickable {
                                        showDatePicker = true
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (tanggalKonsumsi.isNotBlank()) tanggalKonsumsi else "2025-05-20",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextColor
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Select Date",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Chart area
                            if (isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = BiruPrimer)
                                }
                            } else if (dataSudahAda && dataAnalisis != null) {
                                // Show actual chart
                                AsiBarChart(data = dataAnalisis!!)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Belum ada data Asupan Harian yang Moms Isi",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Analysis Result Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hasil Analisis",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextColor
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (dataSudahAda && dataAnalisis != null && usiaAnak.isNotBlank()) {
                                val pesan = generatePesanAnalisis(
                                    usiaAnak = usiaAnak,
                                    standar = dataAnalisis!!.standarFrekuensi,
                                    jumlahPorsi = dataAnalisis!!.jumlahPorsiDikonsumsi
                                )
                                Text(
                                    text = pesan,
                                    fontSize = 14.sp,
                                    color = TextColor,
                                    lineHeight = 20.sp
                                )
                            } else {
                                Text(
                                    text = "Analisis akan muncul setelah data asupan tersedia",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Food Recommendation Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Rekomendasi MPASI Buat Moms Biar Ga Bingung Mau Buat Apa, rekomendasi ini berdasarkan usia si kecil ya Moms!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            when {
                                isLoadingResep -> {
                                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                                }
                                errorMessageResep != null -> {
                                    Text(
                                        text = errorMessageResep ?: "Terjadi kesalahan",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                resepList.isEmpty() -> {
                                    Text(
                                        text = "Belum ada rekomendasi makanan ditemukan.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                else -> {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        items(resepList) { resep ->
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                                modifier = Modifier
                                                    .width(180.dp)
                                                    .wrapContentHeight()
                                                    .clickable {
                                                        navController.navigate("resep_detail/${resep.id_resep}")
                                                    }
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    // Ganti IP sesuai alamat server kamu jika perlu
                                                    val imageUrl = "https://9632-180-244-133-142.ngrok-free.app/storage/resep/${resep.foto_resep}"
                                                    AsyncImage(
                                                        model = imageUrl,
                                                        contentDescription = resep.nama_resep,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .height(100.dp)
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = resep.nama_resep,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error message
                    error?.let { errorMessage ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Date Picker Dialog for ASI section
        if (showDatePicker) {
            rememberDatePickerDialog(
                onDateSelected = {
                    viewModelAsupan.setTanggalKonsumsi(it)
                    showDatePicker = false
                }
            ).show()
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

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                setBackgroundColor(android.graphics.Color.WHITE)
                description.isEnabled = false
                axisRight.isEnabled = false
                legend.isEnabled = true
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

            val konsumsiDataSet = BarDataSet(konsumsiEntries, "Nutrisi Asupan").apply {
                color = android.graphics.Color.parseColor("#0F67FE")
            }
            val standarDataSet = BarDataSet(standarEntries, "Standar Nutrisi").apply {
                color = android.graphics.Color.parseColor("#FF6B6B")
            }

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
            .height(250.dp)
    )
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
        "Anak Moms sekarang berusia sekitar $kategoriUsia, yang dimana harus diberi ASI setidaknya $standar kali sehari, dan dari data, anak Moms sudah memenuhinya. Jangan lupa untuk diberikan ASI dengan tingkat pemberian yang sama di hari berikutnya!"
    } else {
        "Anak Moms sekarang berusia sekitar $kategoriUsia, yang dimana harus diberi ASI setidaknya $standar kali sehari, dan dari data, dapat dilihat bahwa anak Moms belum memenuhi pemberian ASI-nya. Jangan lupa untuk diberikan ASI sesuai dengan standar pemberian ASI pada hari berikutnya!"
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

@Composable
fun ChildProfileHeader(
    viewModel: PertumbuhanViewModel,
    children: List<AnakEntity>,
    selectedChild: AnakEntity?,
    onChildChanged: (Int) -> Unit,
) {
    val childAges by viewModel.childAges.collectAsState()

    Column(modifier = Modifier.padding(20.dp)) {
        Text(
            text = "Profil Anak",
            style = Typography.titleLarge.copy(color = Color(0xFF0A3D62)),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(children) { index, child ->
                val isSelected = child.idAnak == selectedChild?.idAnak

                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .height(100.dp)
                        .clickable { onChildChanged(index) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                            contentDescription = "Foto Anak",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = child.namaAnak,
                                style = Typography.bodyLarge.copy(
                                    color = Color(0xFF212121),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = childAges[child.idAnak]?.let { "$it" } ?: "-",
                                style = Typography.bodySmall.copy(color = Color(0xFF757575))
                            )
                        }

                        if (isSelected) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Terpilih",
                                tint = Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }
        }
    }
}