package com.example.goldencinema // Upewnij się, że nazwa paczki pasuje!

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goldencinema.ui.theme.CinemaGold

@Composable
fun RegisterScreen() {
    // Te zmienne przechowują dane wpisywane przez użytkownika
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Surface to nasze główne tło - identyczne jak w Logowaniu
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212) // Czysta czerń/bardzo ciemny szary
    ) {
        // Column z verticalScroll, żeby użytkownik mógł przewijać, jeśli klawiatura zasłoni pola
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()), // Pozwala przewijać ekran
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. LOGO / NAZWA KINA (To samo co w Logowaniu)
            Text(
                text = "GoldenCinema",
                style = TextStyle(
                    color = CinemaGold,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. NAGŁÓWEK "REJESTRACJA"
            Text(
                text = "Rejestracja",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
                // Usunęliśmy tło pod napisem, żeby było czystsze, jak w logowaniu
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. POLE UŻYTKOWNIKA (Ikona Person, Złoty akcent)
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

            // 4. POLE E-MAIL (Nowe pole, Ikona Email)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Adres E-mail", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CinemaGold) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CinemaGold,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. POLE HASŁA (Ikona Lock, Kropki zamiast tekstu)
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

            Spacer(modifier = Modifier.height(16.dp))

            // 6. POLE POWTÓRZ HASŁO (Nowe pole, Ikona Lock)
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Powtórz hasło", color = Color.Gray) },
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

            // 7. ZŁOTY PRZYCISK (To samo co w Logowaniu)
            Button(
                onClick = { /* Tu później dopiszemy logikę */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
            ) {
                Text(
                    text = "ZAREJESTRUJ",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 8. LINK POMOCNICZY (Powrót do logowania)
            Text(
                text = "Masz już konto? Zaloguj się",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.clickable { /* Tu będzie przejście do logowania */ }
            )
        }
    }
}

// 9. PODGLĄD (Żebyś widział, co kodujesz)
@Preview
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}