package com.example.grow.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.grow.R
import com.example.grow.ui.theme.PoppinsFamily
import com.example.grow.util.SessionManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavHostController,
    onSplashFinished: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current

    // Theme colors
    val lightColor = Color(0xFF63B3FF)
    val secondaryColor = Color(0xFF47A9FF)

    // Gradient background
    val gradient = Brush.verticalGradient(
        colors = listOf(lightColor, secondaryColor),
        startY = 0f,
        endY = 1000f
    )

    // Animation states
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnimation = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )

    val scaleAnimation = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Navigation logic with error handling
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000) // Reduced duration for better UX

        try {
            val isLoggedIn = SessionManager.isLoggedIn(context)
            onSplashFinished(isLoggedIn)
            navController.navigate(if (isLoggedIn) Screen.Home.route else Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        } catch (e: Exception) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    SplashScreenContent(
        gradient = gradient,
        alpha = alphaAnimation.value,
        scale = scaleAnimation.value
    )
}

@Composable
private fun SplashScreenContent(
    gradient: Brush,
    alpha: Float,
    scale: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .alpha(alpha)
                .scale(scale)
        ) {
            // App name
            Text(
                text = "grow.",
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = PoppinsFamily,
                textAlign = TextAlign.Center
            )

            // Tagline
            Text(
                text = "Solusi Cerdas Deteksi\nDini Stunting",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                fontFamily = PoppinsFamily,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Modern progress indicator
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    val lightColor = Color(0xFF63B3FF)
    val secondaryColor = Color(0xFF47A9FF)
    val gradient = Brush.verticalGradient(
        colors = listOf(lightColor, secondaryColor),
        startY = 0f,
        endY = 1000f
    )
    SplashScreenContent(
        gradient = gradient,
        alpha = 1f, // Full opacity for preview
        scale = 1f  // Full scale for preview
    )
}