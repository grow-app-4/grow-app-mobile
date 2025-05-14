package com.example.grow.ui.screen

import androidx.compose.foundation.BorderStroke
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
import com.example.grow.R
import com.example.grow.data.model.Resep
import com.example.grow.viewmodel.ResepViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkResepScreen(
    navController: NavController,
    viewModel: ResepViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }

    // State untuk menyimpan filter yang dipilih
    var selectedTimeFilter by remember { mutableStateOf("All") }
    var selectedRatingFilter by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // Collect bookmarked resep dari repository
    val bookmarkedResepList by viewModel.bookmarkedResepList.collectAsState()

    // Dummy data untuk resep yang dibookmark
    val dummyBookmarkedResepList = remember {
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
                namaMakanan = "Spice roasted chicken with flavored rice",
                chefName = "Mark Kelvin",
                rating = 4.0f,
                imageUrl = R.drawable.ic_launcher_background
            ),
            Resep(
                idMakanan = "3",
                namaMakanan = "Spicy fried rice mix chicken Bali",
                chefName = "Spicy Nelly",
                rating = 4.0f,
                imageUrl = R.drawable.ic_launcher_background
            )
        )
    }

    // Filter resep berdasarkan search query dan filter yang dipilih
    val filteredResepList = bookmarkedResepList.filter {
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
                title = { Text("Bookmark Resep", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
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

                // Filter Button
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

            // Tampilkan filter yang dipilih dalam bentuk chip
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
                                painter = painterResource(id = R.drawable.ic_close), // Tambahkan icon close di resources
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
                                painter = painterResource(id = R.drawable.ic_close),
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
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Hapus filter",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Pencarian Terbaru Text
            Text(
                text = "${filteredResepList.size} hasil",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
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
                        isBookmarked = true, // Karena ini di halaman bookmark, pasti true
                        onBookmarkClick = {
                            viewModel.toggleBookmark(resep) // Menghapus bookmark
                        }
                    )
                }
            }
        }
    }

    // Dialog Filter
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onFilterSelected: (String, Int?, String) -> Unit,
    initialTimeFilter: String,
    initialRatingFilter: Int?,
    initialCategoryFilter: String
) {
    var selectedTime by remember { mutableStateOf(initialTimeFilter) }
    var selectedRating by remember { mutableStateOf(initialRatingFilter) }
    var selectedCategory by remember { mutableStateOf(initialCategoryFilter) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Pencarian", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Time", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Newest", "Oldest", "Popularity").forEach { time ->
                        FilterButton(
                            text = time,
                            isSelected = selectedTime == time,
                            onClick = { selectedTime = time }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Rate", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { rating ->
                        FilterButton(
                            text = "$rating stars",
                            isSelected = selectedRating == rating,
                            onClick = { selectedRating = if (selectedRating == rating) null else rating }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Category", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "All", "Cereal", "Vegetables", "Dinner", "Chinese",
                        "Local Dish", "Fruit", "Breakfast", "Spanish", "Lunch"
                    ).forEach { category ->
                        FilterButton(
                            text = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onFilterSelected(selectedTime, selectedRating, selectedCategory)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1876F2))
            ) {
                Text("Filter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF1876F2) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null,
        modifier = Modifier.height(36.dp)
    ) {
        Text(text)
    }
}