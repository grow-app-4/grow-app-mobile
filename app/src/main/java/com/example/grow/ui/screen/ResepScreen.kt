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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.grow.R
import com.example.grow.data.model.Resep
import com.example.grow.ui.components.FilterDialog
import com.example.grow.viewmodel.ResepViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResepScreen(
    navController: NavController,
    viewModel: ResepViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }

    val bookmarkedResepIds by viewModel.bookmarkedResepIds.collectAsState()
    val resepList by viewModel.resepList.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsState()
    val selectedRatingFilter by viewModel.selectedRatingFilter.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()

    val filteredResepList = viewModel.getFilteredResepList(searchQuery)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cari Resep", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("bookmark_resep") }) {
                        Icon(Icons.Default.Bookmark, "Bookmark List", tint = Color(0xFF1876F2))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari Resep") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search Icon") },
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { showFilterDialog = true },
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1876F2))
                ) {
                    Icon(Icons.Default.Tune, "Filter", tint = Color.White)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTimeFilter != "All") {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.setFilters("All", selectedRatingFilter, selectedCategoryFilter) },
                        label = { Text(selectedTimeFilter) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, "Hapus filter", modifier = Modifier.size(16.dp))
                        }
                    )
                }
                if (selectedRatingFilter != null) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.setFilters(selectedTimeFilter, null, selectedCategoryFilter) },
                        label = { Text("$selectedRatingFilter stars") },
                        trailingIcon = {
                            Icon(Icons.Default.Close, "Hapus filter", modifier = Modifier.size(16.dp))
                        }
                    )
                }
                if (selectedCategoryFilter != "All") {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.setFilters(selectedTimeFilter, selectedRatingFilter, "All") },
                        label = { Text(selectedCategoryFilter) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, "Hapus filter", modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            Text(
                text = "Pencarian Terbaru",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF1876F2)
                        )
                    }
                    error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Error, "Error", tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(error ?: "Terjadi kesalahan", color = Color.Red)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadResep() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1876F2))
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                    filteredResepList.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SearchOff, "No Results", tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tidak ada resep yang ditemukan", color = Color.Gray)
                        }
                    }
                    else -> {
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
                                    onClick = { navController.navigate("resep_detail/${resep.idResep}") },
                                    isBookmarked = bookmarkedResepIds.contains(resep.idResep),
                                    onBookmarkClick = { viewModel.toggleBookmark(resep) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showFilterDialog) {
            FilterDialog(
                onDismiss = { showFilterDialog = false },
                onFilterSelected = { time, rating, category ->
                    viewModel.setFilters(time, rating, category)
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
    println("Total Harga untuk ${resep.namaResep}: ${resep.totalHarga}")

    val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(resep.totalHarga ?: 0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = if (resep.imageUrl != null) {
                    rememberAsyncImagePainter(
                        model = resep.imageUrl,
                        error = painterResource(id = R.drawable.ic_recipe_placeholder)
                    )
                } else {
                    painterResource(id = R.drawable.ic_recipe_placeholder)
                },
                contentDescription = resep.namaResep,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier.fillMaxSize().background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        startY = 0f,
                        endY = 1000f
                    )
                )
            )

            // Rating di kiri atas
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_star),
                    "Rating",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${resep.rating ?: 0f}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }

            // Hapus badge usia rekomendasi di kanan atas
            // IconButton untuk bookmark tetap ada
            IconButton(
                onClick = onBookmarkClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp, top = 8.dp)
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

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    resep.namaResep,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Gabungkan usia rekomendasi dan harga dalam satu baris tanpa label "Rekomendasi:"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        resep.usiaRekomendasi ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        formattedPrice,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}