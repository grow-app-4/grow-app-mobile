package com.example.grow.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ButtonTambahPertumbuhan(
    selectedChildId: Int?,
    onClick: (Int) -> Unit,
    scrollState: ScrollState
) {
    // Mendeteksi arah scroll
    val previousScrollValue = remember { mutableStateOf(0) }
    val isScrollingDown = remember { mutableStateOf(false) }

    // Update scroll direction
    LaunchedEffect(scrollState.value) {
        isScrollingDown.value = scrollState.value > previousScrollValue.value
        previousScrollValue.value = scrollState.value
    }

    // Button akan muncul saat scroll ke atas dan hilang saat scroll ke bawah
    val showButton = remember { mutableStateOf(true) } // Default muncul

    LaunchedEffect(isScrollingDown.value) {
        showButton.value = !isScrollingDown.value // Show when not scrolling down
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Action Button dengan animasi
        AnimatedVisibility(
            visible = showButton.value && selectedChildId != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp),
            enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(animationSpec = tween(300)) { it },
            exit = fadeOut(animationSpec = tween(300)) +
                    slideOutVertically(animationSpec = tween(300)) { it }
        ) {
            Button(
                onClick = { selectedChildId?.let { onClick(it) } },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4285F4) // Warna biru seperti pada gambar
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Data Pertumbuhan Baru",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}