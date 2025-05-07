package com.example.grow.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.FoodBank
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.FoodBank
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    idAnak: Int?
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
            outlinedIcon = Icons.Outlined.ListAlt,
            filledIcon = Icons.Rounded.ListAlt,
            label = "Nutrisi"
        ),
        BottomNavItem(
            screen = Screen.Resep,
            outlinedIcon = Icons.Outlined.FoodBank,
            filledIcon = Icons.Rounded.FoodBank,
            label = "Resep"
        ),
        BottomNavItem(
            screen = Screen.Profile,
            outlinedIcon = Icons.Outlined.Person,
            filledIcon = Icons.Rounded.Person,
            label = "Profile"
        )
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BackgroundColor,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = BackgroundColor,
            contentColor = BiruPrimer,
            tonalElevation = 0.dp
        ) {
            bottomItems.forEach { item ->
                val isSelected = currentRoute == item.screen.route
                val interactionSource = remember { MutableInteractionSource() }

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        val iconSize by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 24.dp,
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            label = "iconSize"
                        )

                        Icon(
                            imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(iconSize)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BiruPrimer,
                        selectedTextColor = BiruPrimer,
                        unselectedIconColor = TextColor.copy(alpha = 0.6f),
                        unselectedTextColor = TextColor.copy(alpha = 0.6f),
                        indicatorColor = BiruPrimer.copy(alpha = 0.1f)
                    ),
                    interactionSource = interactionSource
                )
            }
        }
    }
}