package com.example.grow.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.grow.data.model.FilterCategory
import com.example.grow.data.model.FilterOption

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
@Composable
fun FilterOptionGroup(
    options: List<FilterOption>,
    selectedOptions: List<String>,
    isSingleSelection: Boolean,
    onSelectionChanged: (String, Boolean) -> Unit
) {
    val rows = options.chunked(3) // Mengubah dari 4 ke 3 item per baris agar lebih lega

    Column {
        rows.forEach { rowOptions ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),  // Menambahkan padding vertikal
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowOptions.forEach { option ->
                    val isSelected = selectedOptions.contains(option.id)
                    FilterOptionButton(
                        text = option.displayText,
                        isSelected = isSelected,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onSelectionChanged(option.id, !isSelected)
                        }
                    )
                }

                // Jika row tidak penuh, tambahkan spacer
                if (rowOptions.size < 3) {  // Sesuaikan dengan jumlah item per baris
                    Spacer(modifier = Modifier.weight(3 - rowOptions.size.toFloat()))
                }
            }
        }
    }
}

@Composable
fun FilterOptionButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF1876F2) else Color.White
    val contentColor = if (isSelected) Color.White else Color.Black
    val borderColor = if (isSelected) Color(0xFF1876F2) else Color(0xFFE0E0E0)

    Button(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(100.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ),
        // Menambahkan padding horizontal yang lebih besar agar teks tidak terpotong
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            // Memastikan teks tidak terpotong dengan maxLines dan overflow
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}