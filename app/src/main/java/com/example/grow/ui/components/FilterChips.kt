package com.example.grow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grow.data.model.AppliedFilter

@Composable
fun FilterChips(
    appliedFilters: List<AppliedFilter>,
    onFilterRemoved: (AppliedFilter) -> Unit
) {
    if (appliedFilters.isEmpty()) return

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(appliedFilters) { filter ->
            FilterChip(
                filter = filter,
                onRemove = { onFilterRemoved(filter) }
            )
        }
    }
}

@Composable
fun FilterChip(
    filter: AppliedFilter,
    onRemove: () -> Unit
) {
    // Menggunakan Surface dengan padding yang lebih besar
    Surface(
        modifier = Modifier
            .height(36.dp) // Meningkatkan tinggi
            .clip(RoundedCornerShape(18.dp)), // Memastikan sudut tetap bulat
        color = Color(0xFFE8F3FF)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), // Menambah padding vertikal
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = filter.displayText,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1876F2),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis // Mencegah teks terpotong
            )

            Spacer(modifier = Modifier.width(8.dp)) // Lebih banyak ruang sebelum ikon

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp) // Memperbesar sedikit area klik
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove filter",
                    tint = Color(0xFF1876F2),
                    modifier = Modifier.size(14.dp) // Memperbesar sedikit ikon
                )
            }
        }
    }
}