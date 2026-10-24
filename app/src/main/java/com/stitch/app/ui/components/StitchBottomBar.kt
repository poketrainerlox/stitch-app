package com.stitch.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.stitch.app.ui.navigation.Screen
import com.stitch.app.ui.theme.*

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun StitchBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(Screen.Home.route, "Home", Icons.Rounded.Home),
        BottomNavItem(Screen.Friends.route, "Friends", Icons.Rounded.Group),
        BottomNavItem(Screen.Pacts.route, "Pacts", Icons.Rounded.Lock),
        BottomNavItem(Screen.Schedule.route, "Schedule", Icons.Rounded.CalendarMonth),
        BottomNavItem(Screen.Settings.route, "Settings", Icons.Rounded.Settings)
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Don't show bottom bar on auth or full-screen creation routes
    if (currentRoute == Screen.Login.route ||
        currentRoute == Screen.SignUp.route ||
        currentRoute == Screen.CreatePact.route ||
        currentRoute == Screen.AddFriend.route
    ) {
        return
    }

    NavigationBar(
        containerColor = StitchSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StitchPrimary,
                    selectedTextColor = StitchPrimary,
                    indicatorColor = StitchPrimary.copy(alpha = 0.15f),
                    unselectedIconColor = StitchTextTertiary,
                    unselectedTextColor = StitchTextTertiary
                )
            )
        }
    }
}
