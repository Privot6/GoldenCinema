package com.example.goldencinema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.goldencinema.ui.theme.GoldenCinemaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoldenCinemaTheme {
                val navController = rememberNavController()
                val startDest = if (TokenStore.isTokenValid()) "repertuar" else "login"

                NavHost(navController = navController, startDestination = startDest) {
                    composable("login") {
                        LoginScreen(navController)
                    }
                    composable("register") {
                        RegisterScreen(navController)
                    }
                    composable("repertuar") {
                        RepertuarScreen(navController)
                    }
                    composable("seats/{screeningId}/{basePrice}") { backStack ->
                        val screeningId = backStack.arguments?.getString("screeningId")?.toLong()
                            ?: return@composable
                        val basePrice = backStack.arguments?.getString("basePrice")?.replace(',', '.')?.toDouble() ?: 0.0
                        SeatSelectionScreen(screeningId, basePrice, navController)
                    }
                    composable("my-reservations") {
                        MyReservationsScreen(navController)
                    }
                    composable("profile") {
                        ProfileScreen(navController)
                    }
                }
            }
        }
    }
}
