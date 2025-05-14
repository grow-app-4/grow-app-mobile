package com.example.grow.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.grow.R
import com.example.grow.data.model.Resep
import com.example.grow.viewmodel.ResepViewModel
import com.example.grow.ui.components.FilterDialog
import com.example.grow.ui.components.FilterChips
import com.example.grow.data.model.AppliedFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResepScreen(
    navController: NavController,
    viewModel: ResepViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val bookmarkedResepList by viewModel.bookmarkedResepList.collectAsState()

    // BAGIAN YANG PERLU DIUBAH - Tambahkan state untuk filter
    var showFilterDialog by remember { mutableStateOf(false) }

    // BAGIAN YANG PERLU DIUBAH - Filter state yang sama dengan BookmarkResepScreen
    var selectedTimeFilter by remember { mutableStateOf("All") }
    var selectedRatingFilter by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // Membuat data dummy untuk ditampilkan
    val dummyResepList = remember {
        listOf(
            Resep(
                idMakanan = "1",
                namaMakanan = "Traditional spare ribs baked",
                chefName = "Chef John",
                rating = 4.0f,
                imageUrl = R.drawable.ic_launcher_background
            ),
            Resep(
                idMakanan = "2",
                namaMakanan = "Lamb chops with fruity couscous and mint",
                chefName = "Spicy Nelly",
                rating = 4.0f,
                imageUrl = R.drawable.ic_launcher_background
            ),
            Resep(
                idMakanan = "3",
                namaMakanan = "Spice roasted chicken with flavored rice",
                chefName = "Mark Kelvin",
                rating = 4.0f,
                imageUrl = R.drawable.ic_launcher_background
            ),
            Resep(
                idMakanan = "4",
                namaMakanan = "Chinese style Egg fried rice with sliced pork",
                chefName = "Laura Wilson",
                rating = 4.0f,
                imageUrl = R.drawable.ic_launcher_background
            ),
            // Mengulang data untuk mencapai 8 item seperti di screenshot
            Resep(
                idMakanan = "5",
                namaMakanan = "Lamb chops with fruity couscous and mint",
                chefName = "Spicy Nelly",
                rating = 4.0f,
                imageUrl = R.drawable.ic_launcher_background
            ),
            Resep(
                idMakanan = "6",
                namaMakanan = "Traditional spare ribs baked",
                chefName = "Chef John",
                rating = 4.0f,
                imageUrl = R.drawable.ic_launcher_background
            ),
            Resep(
                idMakanan = "7",
                namaMakanan = "Spice roasted chicken with flavored rice",
                chefName = "Mark Kelvin",
                rating = 4.0f,
                imageUrl = R.drawable.ic_launcher_background
            ),
            Resep(
                idMakanan = "8",
                namaMakanan = "Chinese style Egg fried rice with sliced pork",
                chefName = "Laura Wilson",
                rating = 4.0f,
                imageUrl = R.drawable.ic_launcher_background
            )
        )
    }

    // Menggunakan data dummy, nanti bisa diganti dengan data dari ViewModel
    val resepList = dummyResepList

    val filteredResepList = dummyResepList.filter {
        val matchesSearch = searchQuery.isEmpty() || it.namaMakanan.contains(searchQuery, ignoreCase = true)
        val matchesTime = when (selectedTimeFilter) {
            "Newest" -> true // Logika untuk newest bisa ditambahkan
            "Oldest" -> true // Logika untuk oldest bisa ditambahkan
            "Popularity" -> true // Logika untuk popularity bisa ditambahkan
            else -> true
        }
        val matchesRating = selectedRatingFilter?.let { rating -> it.rating?.toInt() == rating } ?: true
        val matchesCategory = selectedCategoryFilter == "All" || it.namaMakanan.contains(selectedCategoryFilter, ignoreCase = true)

        matchesSearch && matchesTime && matchesRating && matchesCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cari Resep", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    // Tambahkan tombol untuk ke halaman bookmark
                    IconButton(onClick = { navController.navigate(Screen.BookmarkResep.route) }) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Bookmark List",
                            tint = Color(0xFF1876F2)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari Resep") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Filter Button (Blue Button)
                Button(
                    onClick = { showFilterDialog = true },
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1876F2))
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = Color.White
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTimeFilter != "All") {
                    FilterChip(
                        selected = true,
                        onClick = { selectedTimeFilter = "All" },
                        label = { Text(selectedTimeFilter) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus filter",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                if (selectedRatingFilter != null) {
                    FilterChip(
                        selected = true,
                        onClick = { selectedRatingFilter = null },
                        label = { Text("$selectedRatingFilter stars") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus filter",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                if (selectedCategoryFilter != "All") {
                    FilterChip(
                        selected = true,
                        onClick = { selectedCategoryFilter = "All" },
                        label = { Text(selectedCategoryFilter) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus filter",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Pencarian Terbaru Text
            Text(
                text = "Pencarian Terbaru",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            // Grid of Recipe Cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredResepList) { resep ->
                    ResepCard(
                        resep = resep,
                        onClick = {
                            navController.navigate(Screen.ResepDetail.createRoute(resep.idMakanan))
                        },
                        isBookmarked = bookmarkedResepList.any { it.idMakanan == resep.idMakanan },
                        onBookmarkClick = {
                            viewModel.toggleBookmark(resep)
                        }
                    )
                }
            }
        }

        // BAGIAN YANG PERLU DIUBAH - Dialog Filter seperti di BookmarkResepScreen
        if (showFilterDialog) {
            FilterDialog(
                onDismiss = { showFilterDialog = false },
                onFilterSelected = { time, rating, category ->
                    selectedTimeFilter = time
                    selectedRatingFilter = rating
                    selectedCategoryFilter = category
                    showFilterDialog = false
                },
                initialTimeFilter = selectedTimeFilter,
                initialRatingFilter = selectedRatingFilter,
                initialCategoryFilter = selectedCategoryFilter
            )
        }
    }
}

@Composable
fun ResepCard(
    resep: Resep,
    onClick: () -> Unit,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Food Image
            Image(
                painter = painterResource(id = resep.imageUrl ?: R.drawable.ic_star),
                contentDescription = resep.namaMakanan,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay untuk teks
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            ),
                            startY = 0f,
                            endY = 1000f
                        )
                    )
            )

            // Rating Badge with star icon
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = "Rating",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${resep.rating}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }

            // Bookmark Icon - Tambahkan di posisi top end
            IconButton(
                onClick = onBookmarkClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) Color(0xFF1876F2) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Food Title and Chef Name
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = resep.namaMakanan,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "By ${resep.chefName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}