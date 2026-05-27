package com.example.goldencinema

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.goldencinema.ui.theme.CinemaGold

@Composable
fun MainBottomBar(navController: NavController, currentRoute: String) {
    val isEmployee = remember {
        val role = TokenStore.getUserRole()
        role == "EMPLOYEE" || role == "ADMIN"
    }

    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = CinemaGold,
        selectedTextColor = CinemaGold,
        indicatorColor = Color(0xFF1E1E1E),
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray
    )

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
            label = { Text(stringResource(R.string.nav_repertuar)) },
            colors = itemColors
        )
        if (!isEmployee) {
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
                label = { Text(stringResource(R.string.nav_reservations)) },
                colors = itemColors
            )
        }
        if (isEmployee) {
            NavigationBarItem(
                selected = currentRoute == "scanner",
                onClick = {
                    if (currentRoute != "scanner") {
                        navController.navigate("scanner") {
                            popUpTo("repertuar") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_scanner)) },
                colors = itemColors
            )
        }
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile") {
                        popUpTo("repertuar") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_profile)) },
            colors = itemColors
        )
    }
}
