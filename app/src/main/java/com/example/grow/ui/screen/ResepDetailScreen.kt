package com.example.grow.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.grow.R
import com.example.grow.data.model.Resep
import com.example.grow.viewmodel.ResepDetailViewModel
val bookmarkedResepList = mutableStateListOf<Resep>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResepDetailScreen(
    resepId: String,
    navController: NavController,
    viewModel: ResepDetailViewModel = hiltViewModel()
) {
    // Add the missing state variable
    var selectedTab by remember { mutableStateOf(0) }

    // Nantinya akan memuat detail resep berdasarkan ID
    LaunchedEffect(resepId) {
        viewModel.loadResepDetail(resepId)
    }

    // Cek status bookmark
    val bookmarkedResepList by viewModel.bookmarkedResepList.collectAsState()
    val isBookmarked = bookmarkedResepList.any { it.idMakanan == resepId }

    // Cek apakah resep sudah dibookmark
    val resep = Resep(
        idMakanan = resepId,
        namaMakanan = "Spicy chicken burger with French fries",
        chefName = "Spicy Nelly",
        rating = 4.0f,
        imageUrl = R.drawable.ic_launcher_background
    )

    // Dummy data untuk pengujian UI
    val namaMakanan = "Spicy chicken burger with French fries"
    val reviews = "13k Reviews"
    val cookTime = "20 min"
    val rating = 4.0f
    val nutritionInfo = listOf(
        NutritionInfo("Carbs", "65g", R.drawable.ic_star),
        NutritionInfo("Proteins", "27g", R.drawable.ic_star),
        NutritionInfo("Carbs", "65g", R.drawable.ic_star),
        NutritionInfo("Proteins", "27g", R.drawable.ic_star),
        NutritionInfo("Calories", "120 Kcal", R.drawable.ic_star),
        NutritionInfo("Fats", "91g", R.drawable.ic_star)
    )

    val ingredients = listOf(
        IngredientItem("Tomatos", "500g", R.drawable.ic_star),
        IngredientItem("Cabbage", "300g", R.drawable.ic_star),
        IngredientItem("Lettuce", "200g", R.drawable.ic_star),
        IngredientItem("Chicken", "500g", R.drawable.ic_star),
        IngredientItem("Bread", "2 pcs", R.drawable.ic_star),
        IngredientItem("Cheese", "100g", R.drawable.ic_star),
        IngredientItem("Potatoes", "400g", R.drawable.ic_star)
    )

    val steps = listOf(
        "Siapkan semua bahan-bahan yang diperlukan untuk burger ayam pedas.",
        "Marinasi ayam dengan bumbu pedas. Campurkan bubuk cabai, garam, merica, bawang putih bubuk, dan sedikit minyak zaitun. Diamkan selama minimal 30 menit.",
        "Panaskan wajan atau grill. Masak ayam yang sudah dimarinasi hingga matang sempurna dan berwarna kecoklatan.",
        "Potong sayuran untuk topping burger: iris tipis tomat, cabbage, dan bahan lainnya.",
        "Panggang roti burger hingga sedikit kecoklatan di kedua sisi.",
        "Susun burger dengan urutan: roti bagian bawah, saus, sayuran, daging ayam pedas, keju, dan roti bagian atas.",
        "Untuk french fries, potong kentang memanjang, rendam dalam air dingin selama 30 menit, keringkan, dan goreng hingga kecoklatan dan garing.",
        "Sajikan burger ayam pedas dengan french fries di samping.",
        "Nikmati selagi hangat!"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark(resep) }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFF1876F2) else Color.Gray
                        )
                    }
                    IconButton(onClick = { /* Menu options */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Gambar Makanan dengan Rating dan Timer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background), // Ganti dengan gambar burger
                    contentDescription = namaMakanan,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Rating badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700), // Gold color
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$rating",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }

                // Timer badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Timer text
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = "Timer",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cookTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }

            // Informasi Resep
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = namaMakanan,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "($reviews)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Grid Informasi Nutrisi
                LazyGridForNutrition(nutritionInfo)

                Spacer(modifier = Modifier.height(24.dp))

                // Tab untuk Bahan dan Langkah
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    TabButton(
                        title = "Bahan",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        title = "Langkah - langkah",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Serving info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = "Serving",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1 serve",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (selectedTab == 0) {
                        Text(
                            text = "10 Items",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = "10 Steps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content berdasarkan tab yang dipilih
                if (selectedTab == 0) {
                    // Tab Bahan
                    IngredientsList(ingredients)
                } else {
                    // Tab Langkah-langkah
                    StepsList(steps)
                }
            }
        }
    }
}

@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF1876F2) else Color.LightGray.copy(alpha = 0.3f),
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(48.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun LazyGridForNutrition(nutritionInfo: List<NutritionInfo>) {
    Column {
        for (i in 0 until nutritionInfo.size step 2) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Setiap baris berisi 2 item
                for (j in 0 until 2) {
                    if (i + j < nutritionInfo.size) {
                        val info = nutritionInfo[i + j]
                        NutritionItem(
                            icon = info.iconResource,
                            label = info.label,
                            value = info.value,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            if (i + 2 < nutritionInfo.size) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NutritionItem(
    icon: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = Color(0xFF2D3648),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun IngredientsList(ingredients: List<IngredientItem>) {
    Column {
        ingredients.forEach { ingredient ->
            IngredientRow(
                icon = ingredient.iconResource,
                name = ingredient.name,
                amount = ingredient.amount
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun IngredientRow(
    icon: Int,
    name: String,
    amount: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = name,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = amount,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun StepsList(steps: List<String>) {
    Column {
        steps.forEachIndexed { index, step ->
            StepRow(
                stepNumber = index + 1,
                description = step
            )
            if (index < steps.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StepRow(
    stepNumber: Int,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text(
            text = "Step $stepNumber",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

// Data class untuk info nutrisi
data class NutritionInfo(
    val label: String,
    val value: String,
    val iconResource: Int
)

// Data class untuk item bahan
data class IngredientItem(
    val name: String,
    val amount: String,
    val iconResource: Int
)