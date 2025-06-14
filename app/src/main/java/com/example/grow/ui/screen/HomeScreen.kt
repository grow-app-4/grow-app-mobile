import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.grow.ui.theme.*
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.grow.data.AnakEntity
import com.example.grow.ui.screen.ButtonTambahPertumbuhan
import com.example.grow.ui.screen.GrafikPertumbuhanScreen
import com.example.grow.ui.screen.Screen
import com.example.grow.ui.viewmodel.PertumbuhanViewModel
import com.example.grow.util.formatTanggalToIndo
import com.example.grow.viewmodel.GrafikViewModel
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: PertumbuhanViewModel = hiltViewModel(),
    userId: Int
) {
    val children by viewModel.children.collectAsState()
    val selectedChildIndex by viewModel.selectedChildIndex.collectAsState()
    val selectedChild = children.getOrNull(selectedChildIndex)
    val statusStunting by viewModel.statusStunting.collectAsState()
    val isLoading by viewModel.isLoadingChildren.collectAsState()
    val isEmptyChildren by viewModel.isEmptyChildren.collectAsState()
    val addAnakStatus by viewModel.addAnakStatus.collectAsState()
    val scrollState = rememberScrollState()

    Log.d("HOME_SCREEN", "userId: $userId, children: $children, selectedChild: $selectedChild, statusStunting: $statusStunting")

    LaunchedEffect(userId) {
        viewModel.loadChildren(userId)
    }

    LaunchedEffect(selectedChild?.idAnak) {
        selectedChild?.idAnak?.let { idAnak ->
            viewModel.loadLatestPertumbuhan(idAnak)
            viewModel.loadStatusStunting(idAnak)
        }
    }

    LaunchedEffect(addAnakStatus, children) {
        if (addAnakStatus is PertumbuhanViewModel.AddAnakStatus.Success) {
            val newAnakId = (addAnakStatus as PertumbuhanViewModel.AddAnakStatus.Success).idAnak
            val newIndex = children.indexOfFirst { it.idAnak == newAnakId }
            if (newIndex != -1) {
                viewModel.selectChild(newIndex)
                Log.d("HOME_SCREEN", "Memilih anak baru dengan idAnak: $newAnakId, index: $newIndex, nama: ${children[newIndex].namaAnak}")
            } else {
                Log.w("HOME_SCREEN", "Anak baru dengan idAnak: $newAnakId belum ada di daftar children: $children")
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // Keep the white background for the top bar
            ) {
                ChildProfileHeader(
                    navController = navController,
                    viewModel = viewModel,
                    children = children,
                    selectedChild = selectedChild,
                    onChildChanged = { selectedIndex ->
                        viewModel.selectChild(selectedIndex)
                    },
                    userId = userId
                )
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BiruMudaMain)
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = BiruPrimer,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .background(
                                BiruMudaMain,
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                        ) {
                            // Always display cards, even if selectedChild is null
                            GrowthDataCard(viewModel = viewModel, idAnak = selectedChild?.idAnak)
                            GrowthChartCard(navController = navController, viewModel = viewModel, anak = selectedChild)
                            AnalysisResultCard(statusStunting = if (selectedChild != null) statusStunting else null)
                            Spacer(modifier = Modifier.height(180.dp))
                        }
                    }

                    selectedChild?.idAnak?.let { idAnak ->
                        ButtonTambahPertumbuhan(
                            selectedChildId = idAnak,
                            onClick = { navController.navigate(Screen.InputDataPertumbuhan.createRoute(idAnak)) },
                            scrollState = scrollState
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ChildProfileHeader(
    navController: NavController,
    viewModel: PertumbuhanViewModel,
    children: List<AnakEntity>,
    selectedChild: AnakEntity?,
    onChildChanged: (Int) -> Unit,
    userId: Int
) {
    val childAges by viewModel.childAges.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Profil Anak",
            style = Typography.titleLarge.copy(color = BiruPrimer),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow {
            if (children.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .width(140.dp)
                            .height(80.dp)
                            .padding(end = 8.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { navController.navigate(Screen.TambahAnak.createRoute(userId)) }
                        ,
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Anak",
                                tint = BiruText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tambah Anak",
                                style = Typography.bodyMedium.copy(color = BiruText)
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(children) { index, child ->
                    val isSelected = child.idAnak == selectedChild?.idAnak

                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .height(80.dp)
                            .padding(end = 8.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onChildChanged(index) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFCCE5FF) else Color.White
                        ),
                        border = if (isSelected) BorderStroke(2.dp, Color(0xFF005B9F)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = child.namaAnak,
                                    style = Typography.bodyMedium.copy(color = Color.Black)
                                )
                                Text(
                                    text = childAges[child.idAnak]?.let { "$it" } ?: "-",
                                    style = Typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                        }
                    }
                }

                // Card untuk tambah anak setelah list anak
                item {
                    Card(
                        modifier = Modifier
                            .width(140.dp)
                            .height(80.dp)
                            .padding(end = 8.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { navController.navigate(Screen.TambahAnak.createRoute(userId)) }
                        ,
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Anak",
                                tint = BiruText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tambah Anak",
                                style = Typography.bodyMedium.copy(color = BiruText)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GrowthDataCard(viewModel: PertumbuhanViewModel = hiltViewModel(), idAnak: Int?) {
    val latestPertumbuhan by viewModel.latestPertumbuhan.collectAsState()

    LaunchedEffect(idAnak) {
        if (idAnak != null) {
            viewModel.loadLatestPertumbuhan(idAnak)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = BackgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GrowthDataItem(
                    title = "Berat Badan",
                    value = latestPertumbuhan?.beratBadan?.let { "$it kg" } ?: "-"
                )
                GrowthDataItem(
                    title = "Tinggi Badan",
                    value = latestPertumbuhan?.tinggiBadan?.let { "$it cm" } ?: "-"
                )
                GrowthDataItem(
                    title = "Lingkar Kepala",
                    value = latestPertumbuhan?.lingkarKepala?.let { "$it cm" } ?: "-"
                )
            }

            Text(
                text = latestPertumbuhan?.tanggalPencatatan?.let { "Data ${formatTanggalToIndo(it)}" } ?: "Belum ada data",
                style = Typography.labelSmall.copy(color = TextColor.copy(alpha = 0.6f)),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun GrowthDataItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = Typography.labelSmall,
            color = TextColor.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = Typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
fun GrowthChartCard(
    navController: NavController,
    viewModel: PertumbuhanViewModel,
    anak: AnakEntity?
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    val jenisOptions = listOf(
        "Berat Badan Sesuai Usia",
        "Tinggi Badan Sesuai Usia",
        "Lingkar Kepala Sesuai Usia"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedJenis by remember { mutableStateOf(jenisOptions[0]) }

    val idJenis = when (selectedJenis) {
        "Tinggi Badan Sesuai Usia" -> 1
        "Berat Badan Sesuai Usia" -> 2
        "Lingkar Kepala Sesuai Usia" -> 3
        else -> 2
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = BackgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = null,
                        tint = BiruText
                    )
                    Text(
                        text = selectedJenis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        style = Typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = TextColor
                        )
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = TextColor.copy(alpha = 0.6f)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(BackgroundColor, shape = RoundedCornerShape(8.dp))
                ) {
                    jenisOptions.forEach { option ->
                        DropdownMenuItem(
                            onClick = {
                                selectedJenis = option
                                expanded = false
                            },
                            text = {
                                Text(
                                    text = option,
                                    style = Typography.bodyMedium.copy(color = TextColor),
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.padding(top = 16.dp),
                contentColor = BiruPrimer,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(3.dp)
                            .background(BiruPrimer)
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Grafik WHO", style = Typography.labelSmall) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Riwayat", style = Typography.labelSmall) }
                )
            }

            // Isi konten tab
            if (anak == null) {
                Text(
                    text = "Pilih atau tambahkan anak terlebih dahulu",
                    style = Typography.bodyMedium.copy(color = TextColor.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else {
                when (selectedTabIndex) {
                    0 -> key(idJenis) {
                        GrowthChart(anak = anak, idJenis = idJenis)
                    }
                    1 -> GrowthHistoryTable(navController = navController, idAnak = anak.idAnak)
                }
            }
        }
    }
}

@Composable
fun GrowthChart(
    anak: AnakEntity,
    idJenis: Int,
    viewModel: GrafikViewModel = hiltViewModel()
) {
    val grafikWHO by viewModel.grafikWHO.collectAsState()
    val grafikAnak by viewModel.grafikAnak.collectAsState()
    val labelSumbuY = when (idJenis) {
        1 -> "Tinggi (cm)"
        2 -> "Berat (kg)"
        3 -> "Lingkar Kepala (cm)"
        else -> "Nilai Pertumbuhan"
    }

    LaunchedEffect(Unit) {
        viewModel.loadGrafik(anak, idJenis)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = BiruPrimer,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Grafik WHO digunakan untuk anak usia 0 - 5 tahun.",
                style = Typography.labelSmall,
                color = TextColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Text(
            text = labelSumbuY,
            style = Typography.labelSmall,
            color = TextColor.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .border(1.dp, BiruPrimer)
                .padding(8.dp)
        ) {
            when {
                grafikWHO.isEmpty() -> {
                    Text(
                        text = "Data standar WHO belum tersedia.\nSilakan lakukan sinkronisasi terlebih dahulu.",
                        style = Typography.bodySmall,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                grafikAnak.isEmpty() -> {
                    // ✅ WHO ada, tapi data anak belum ada
                    Text(
                        text = "Belum ada data pertumbuhan anak.",
                        style = Typography.bodySmall,
                        color = TextColor.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    // ✅ Siap tampilkan grafik
                    GrafikPertumbuhanScreen(
                        anak = anak,
                        idJenis = idJenis,
                        viewModel = viewModel
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(BiruPrimer)
            )
            Text(
                text = "Data Pertumbuhan Anak",
                style = Typography.labelSmall,
                color = TextColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun GrowthHistoryTable(
    navController: NavController,
    idAnak: Int,
    pertumbuhanViewModel: PertumbuhanViewModel = hiltViewModel(),
    grafikViewModel: GrafikViewModel = hiltViewModel()
) {
    val pertumbuhanList by pertumbuhanViewModel.pertumbuhanList.collectAsState()
    var growthHistory by remember { mutableStateOf<List<GrowthHistoryItem>>(emptyList()) }

    // Load data pertumbuhan
    LaunchedEffect(idAnak) {
        pertumbuhanViewModel.getPertumbuhanAnak(idAnak)
    }

    // Transform jadi GrowthHistoryItem
    LaunchedEffect(pertumbuhanList) {
        val anak = grafikViewModel.getAnakById(idAnak).firstOrNull()
        anak?.let {
            growthHistory = pertumbuhanList.map { p ->
                val usia = grafikViewModel.hitungUsiaDalamBulan(it.tanggalLahir, p.pertumbuhan.tanggalPencatatan)?.toString() ?: "-"
                val berat = p.details.find { it.jenis.idJenis == 2 }?.detail?.nilai
                val tinggi = p.details.find { it.jenis.idJenis == 1 }?.detail?.nilai
                val lingkar = p.details.find { it.jenis.idJenis == 3 }?.detail?.nilai

                GrowthHistoryItem(
                    idPertumbuhan = p.pertumbuhan.idPertumbuhan,
                    date = p.pertumbuhan.tanggalPencatatan,
                    ageMonths = usia,
                    weight = berat,
                    height = tinggi,
                    headCircumference = lingkar
                )
            }.sortedByDescending { it.date }
        }
    }

    // Tampilkan UI
    if (growthHistory.isEmpty()) {
        Text("Belum ada data pertumbuhan.")
    } else {
        GrowthHistoryContent(
            growthHistoryData = growthHistory,
            onEdit = { item ->
                navController.navigate(Screen.EditDataPertumbuhan.createRoute(idAnak, item.idPertumbuhan))
            },
            onDelete = { item ->
                pertumbuhanViewModel.deletePertumbuhan(item.idPertumbuhan, idAnak)
            }
        )
    }
}

// Data class for growth history items
data class GrowthHistoryItem(
    val idPertumbuhan: Int,
    val date: String,
    val ageMonths: String,
    val weight: Float?,
    val height: Float?,
    val headCircumference: Float?
)

@Composable
fun GrowthHistoryContent(
    growthHistoryData: List<GrowthHistoryItem>,
    onEdit: (GrowthHistoryItem) -> Unit,
    onDelete: (GrowthHistoryItem) -> Unit
) {
    // State untuk mengelola dialog konfirmasi
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<GrowthHistoryItem?>(null) }

    // State lokal untuk daftar pertumbuhan, memungkinkan pembaruan langsung
    var localGrowthHistory by remember { mutableStateOf(growthHistoryData) }

    // Sinkronkan localGrowthHistory dengan growthHistoryData ketika data berubah
    LaunchedEffect(growthHistoryData) {
        localGrowthHistory = growthHistoryData
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BiruMudaSecondary)
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tanggal",
                style = Typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = BiruText,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Usia\n(bulan)",
                style = Typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = BiruText,
                modifier = Modifier.weight(0.6f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Berat\n(kg)",
                style = Typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = BiruText,
                modifier = Modifier.weight(0.6f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tinggi\n(cm)",
                style = Typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = BiruText,
                modifier = Modifier.weight(0.6f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Lingkar Kepala\n(cm)",
                style = Typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = BiruText,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(60.dp)) // Space for edit + delete buttons
        }

        // Table rows
        localGrowthHistory.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.date,
                    style = Typography.bodySmall,
                    color = TextColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = item.ageMonths,
                    style = Typography.bodySmall,
                    color = TextColor,
                    modifier = Modifier.weight(0.6f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = item.weight?.toString() ?: "-",
                    style = Typography.bodySmall,
                    color = TextColor,
                    modifier = Modifier.weight(0.6f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = item.height?.toString() ?: "-",
                    style = Typography.bodySmall,
                    color = TextColor,
                    modifier = Modifier.weight(0.6f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = item.headCircumference?.toString() ?: "-",
                    style = Typography.bodySmall,
                    color = TextColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onEdit(item) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = BiruPrimer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            itemToDelete = item
                            showDeleteDialog = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Divider(
                color = TextColor.copy(alpha = 0.1f),
                thickness = 1.dp
            )
        }

        // Dialog konfirmasi penghapusan
        if (showDeleteDialog && itemToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    itemToDelete = null
                },
                title = {
                    Text(
                        text = "Konfirmasi Hapus",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextColor
                    )
                },
                text = {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus data pertumbuhan untuk tanggal ${itemToDelete?.date}?",
                        style = Typography.bodyMedium,
                        color = TextColor
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            itemToDelete?.let { item ->
                                // Hapus item dari daftar lokal untuk pembaruan UI langsung
                                localGrowthHistory = localGrowthHistory.filter { it.idPertumbuhan != item.idPertumbuhan }
                                // Panggil fungsi delete dari ViewModel
                                onDelete(item)
                            }
                            showDeleteDialog = false
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = BiruPrimer)
                    ) {
                        Text("Batal")
                    }
                },
                containerColor = BackgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}


@Composable
fun AnalysisResultCard(statusStunting: String?) {
    var isExpanded by remember { mutableStateOf(false) } // State untuk expand/collapse

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }, // Toggle expand/collapse
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hasil Analisis",
                    style = Typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextColor
                    )
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextColor.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pesan utama berdasarkan status
            Text(
                text = when (statusStunting) {
                    "Normal" -> "Yay! Tinggi badan anak sesuai dengan standar usianya. Pertahankan pola makan dan gaya hidup sehat ya! 😊"
                    "Pendek" -> "Tinggi badan anak sedikit di bawah rata-rata. Yuk, lanjutkan pantau pertumbuhannya dan pastikan asupan gizi seimbang! 🌱"
                    "Sangat Pendek" -> "Tinggi badan anak cukup jauh di bawah rata-rata. Kami sarankan konsultasi dengan dokter atau ahli gizi untuk dukungan lebih lanjut. 💙"
                    "Sangat Tinggi" -> "Wow, anak memiliki tinggi badan di atas rata-rata! Pertahankan pola hidup sehat untuk mendukung pertumbuhannya! 🌟"
                    else -> "Belum ada hasil analisis. Yuk, masukkan data tinggi badan anak untuk mengetahui status pertumbuhannya! 📏"
                },
                style = Typography.bodyMedium.copy(color = TextColor),
                modifier = Modifier.animateContentSize() // Animasi halus saat expand/collapse
            )

            // Detail tambahan saat expanded
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (statusStunting) {
                        "Normal" -> "Pertumbuhan tinggi badan anak Ibu/Bapak berada dalam kisaran yang sehat menurut standar WHO. Tetap jaga pola makan bergizi seimbang dan aktivitas fisik yang cukup, ya."
                        "Pendek" -> "Saat ini tinggi badan anak sedikit di bawah rata-rata (z-score < -2). Tidak perlu khawatir, yuk bantu dengan memberikan asupan gizi yang baik dan rutin memantau tumbuh kembangnya."
                        "Sangat Pendek" -> "Tinggi badan anak berada cukup jauh di bawah standar (z-score < -3). Sebaiknya segera berkonsultasi dengan tenaga kesehatan untuk mendapatkan arahan yang tepat dan dukungan lebih lanjut."
                        "Tinggi di Atas Rata-Rata" -> "Tinggi badan anak lebih tinggi dari rata-rata (z-score > +2). Ini hal yang masih dalam batas wajar, namun tetap pantau pertumbuhannya agar tetap seimbang dan sehat."
                        "Sangat Tinggi" -> "Anak memiliki tinggi badan yang jauh di atas rata-rata usianya (z-score > +3). Meskipun biasanya tidak menjadi masalah, ada baiknya sesekali berkonsultasi agar pertumbuhan anak tetap optimal."
                        else -> "Data tinggi badan anak belum tersedia atau belum lengkap. Silakan lengkapi datanya terlebih dahulu agar kami bisa memberikan informasi yang sesuai."
                    },
                    style = Typography.bodySmall.copy(color = TextColor.copy(alpha = 0.8f)),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
