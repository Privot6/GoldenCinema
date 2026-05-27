package com.example.goldencinema

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.goldencinema.ui.theme.GoldenCinemaTheme

class MainActivity : ComponentActivity() {
    private var navControllerRef: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoldenCinemaTheme {
                val navController = rememberNavController().also { navControllerRef = it }
                val startDest = startDestinationFromIntent(intent)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (isPaymentDeepLink(intent)) {
            navControllerRef?.navigate("my-reservations") {
                popUpTo("repertuar") { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    private fun startDestinationFromIntent(intent: Intent): String {
        if (!TokenStore.isTokenValid()) return "login"
        return if (isPaymentDeepLink(intent)) "my-reservations" else "repertuar"
    }

    private fun isPaymentDeepLink(intent: Intent): Boolean =
        intent.data?.scheme == "goldencinema" && intent.data?.host == "payment"
}
