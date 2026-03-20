package com.example.goldencinema // Nie zmieniać

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goldencinema.ui.theme.CinemaGold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.clickable
@Composable
fun LoginScreen() {
    // Te zmienne przechowują to, co wpisujemy w pola
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Surface to nasze główne tło - jednolite i ciemne
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212) // Czysta czerń/bardzo ciemny szary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. LOGO / NAZWA KINA
            Text(
                text = "GoldenCinema",
                style = TextStyle(
                    color = CinemaGold,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Logowanie",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. POLE UŻYTKOWNIKA
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nazwa użytkownika", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CinemaGold) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CinemaGold,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. POLE HASŁA
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(), // Zamienia tekst na kropki
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CinemaGold) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CinemaGold,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. ZŁOTY PRZYCISK
            Button(
                onClick = { /* Tu później dopiszemy logikę */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
            ) {
                Text("ZALOGUJ", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. LINKI POMOCNICZE
            Text(
                text = "Nie masz konta? Zarejestruj się",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.clickable { /* Tu będzie przejście do rejestracji */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}