package com.example.grow.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import coil.compose.rememberAsyncImagePainter
import com.example.grow.R
import com.example.grow.data.model.Resep
import com.example.grow.ui.components.FilterDialog
import com.example.grow.viewmodel.ResepViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkResepScreen(
    navController: NavController,
    viewModel: ResepViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }

    var selectedTimeFilter by remember { mutableStateOf("All") }
    var selectedRatingFilter by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val bookmarkedResepIds by viewModel.bookmarkedResepIds.collectAsState()
    val resepList by viewModel.resepList.collectAsState()

    val filteredResepList = resepList.filter {
        bookmarkedResepIds.contains(it.idResep) &&
                (searchQuery.isEmpty() || it.namaResep.contains(searchQuery, ignoreCase = true)) &&
                (selectedTimeFilter == "All" || (selectedTimeFilter == "Popularity" && it.rating?.let { r -> r >= 4.0 } ?: false)) &&
                (selectedRatingFilter?.let { rating -> it.rating?.toInt() == rating } ?: true) &&
                (selectedCategoryFilter == "All" || it.namaKategori == selectedCategoryFilter)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmark Resep", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
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
                        onClick = { selectedTimeFilter = "All" },
                        label = { Text(selectedTimeFilter) },
                        trailingIcon = {
                            Icon(painterResource(id = R.drawable.ic_close), "Hapus filter", modifier = Modifier.size(16.dp))
                        }
                    )
                }
                if (selectedRatingFilter != null) {
                    FilterChip(
                        selected = true,
                        onClick = { selectedRatingFilter = null },
                        label = { Text("$selectedRatingFilter stars") },
                        trailingIcon = {
                            Icon(painterResource(id = R.drawable.ic_close), "Hapus filter", modifier = Modifier.size(16.dp))
                        }
                    )
                }
                if (selectedCategoryFilter != "All") {
                    FilterChip(
                        selected = true,
                        onClick = { selectedCategoryFilter = "All" },
                        label = { Text(selectedCategoryFilter) },
                        trailingIcon = {
                            Icon(painterResource(id = R.drawable.ic_close), "Hapus filter", modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            Text(
                text = "${filteredResepList.size} hasil",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

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
                        isBookmarked = true,
                        onBookmarkClick = { viewModel.toggleBookmark(resep) }
                    )
                }
            }
        }

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