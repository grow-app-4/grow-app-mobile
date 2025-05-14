package com.example.grow.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.grow.ui.theme.BackgroundColor
import com.example.grow.ui.theme.BiruPrimer
import com.example.grow.ui.theme.TextColor

data class BottomNavItem(
    val screen: Screen,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector,
    val label: String
)

@Composable
fun BottomNavigationWithFab(
    navController: NavController,
    currentRoute: String?,
    idAnak: Int
) {
    val bottomItems = listOf(
        BottomNavItem(
            screen = Screen.Home,
            outlinedIcon = Icons.Outlined.Home,
            filledIcon = Icons.Rounded.Home,
            label = "Home"
        ),
        BottomNavItem(
            screen = Screen.Nutrisi,
            outlinedIcon = Icons.Outlined.AccountBox,
            filledIcon = Icons.Rounded.AccountBox,
            label = "Nutrisi"
        ),
        BottomNavItem(
            screen = Screen.Resep,
            outlinedIcon = Icons.Outlined.Notifications,
            filledIcon = Icons.Rounded.Notifications,
            label = "Resep"
        ),
        BottomNavItem(
            screen = Screen.BookmarkResep,
            outlinedIcon = Icons.Outlined.Person,
            filledIcon = Icons.Rounded.Person,
            label = "Profile"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        BottomAppBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            containerColor = BackgroundColor,
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bottomItems.forEach { item ->
                    // Check if the current route matches the base route
                    // This handles nested routes like resep_detail/1 still highlighting the Resep tab
                    val isSelected = when {
                        currentRoute == item.screen.route -> true
                        item.screen == Screen.Resep && currentRoute?.startsWith("resep_detail") == true -> true
                        else -> false
                    }

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) BiruPrimer else TextColor.copy(alpha = 0.6f),
                        animationSpec = tween(300), label = "iconColor"
                    )
                    val iconSize by animateDpAsState(
                        targetValue = if (isSelected) 26.dp else 24.dp,
                        animationSpec = tween(300), label = "iconSize"
                    )

                    BottomNavItem(
                        icon = if (isSelected) item.filledIcon else item.outlinedIcon,
                        label = item.label,
                        selected = isSelected,
                        iconColor = iconColor,
                        iconSize = iconSize,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // FAB with improved design
        FloatingActionButton(
            onClick = {
                navController.navigate(Screen.InputDataPertumbuhan.createRoute(idAnak)) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            },
            containerColor = BiruPrimer,
            contentColor = BackgroundColor,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-22).dp)
                .size(56.dp)
                .shadow(elevation = 8.dp, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    iconColor: androidx.compose.ui.graphics.Color,
    iconSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) {
                    Modifier.background(
                        BiruPrimer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            )
            .padding(vertical = 8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(iconSize + 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(iconSize)
            )
        }

        Text(
            text = label,
            color = iconColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}