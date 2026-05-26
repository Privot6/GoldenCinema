package com.example.goldencinema

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.goldencinema.ui.theme.CinemaGold

@Composable
fun MainBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = Color.Black) {
        NavigationBarItem(
            selected = currentRoute == "repertuar",
            onClick = {
                if (currentRoute != "repertuar") {
                    navController.navigate("repertuar") {
                        popUpTo("repertuar") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
            icon = { Icon(Icons.Default.Theaters, contentDescription = null) },
            label = { Text(stringResource(R.string.repertuar_title)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CinemaGold,
                selectedTextColor = CinemaGold,
                indicatorColor = Color(0xFF1E1E1E),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            selected = currentRoute == "my-reservations",
            onClick = {
                if (currentRoute != "my-reservations") {
                    navController.navigate("my-reservations") {
                        popUpTo("repertuar") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
            icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
            label = { Text(stringResource(R.string.my_reservations_title)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CinemaGold,
                selectedTextColor = CinemaGold,
                indicatorColor = Color(0xFF1E1E1E),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}
