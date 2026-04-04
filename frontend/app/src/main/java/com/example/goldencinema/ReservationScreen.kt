package com.example.goldencinema

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goldencinema.ui.theme.CinemaGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen() {
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Rezerwacja", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Powrót */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = CinemaGold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            // 1. GÓRNA SEKCJA: PLAKAT I TYTUŁ
            Row(modifier = Modifier.fillMaxWidth()) {
                // Miejsce na plakat
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 140.dp)
                        .background(Color.DarkGray, RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Dune: Part Two",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Akcja, Sci-Fi | 166 min",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // 2. SZCZEGÓŁY REZERWACJI
            ReservationDetail(label = "Godzina:", value = "17:30")
            ReservationDetail(label = "Rząd:", value = "7, Miejsca: 8, 9")

            Spacer(modifier = Modifier.height(16.dp))

            // Cena (obliczona)
            Row {
                Text("Cena: ", color = Color.White, fontSize = 16.sp)
                Text("2 x 25 zł = ", color = CinemaGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("50 zł", color = CinemaGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // 3. OPIS FILMU (Zamiast imienia i nazwiska)
            Text("Opis filmu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paul Atryda jednoczy się z Chani i Fremenami, szukając zemsty na spiskowcach, którzy zniszczyli jego rodzinę. Staje przed wyborem między miłością a losem wszechświata.",
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. POLE E-MAIL
            Text("E-mail", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("twoj@email.com", color = Color.DarkGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CinemaGold,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedContainerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.weight(1f)) // Pcha przycisk na dół

            // 5. PRZYCISK ZATWIERDŹ
            Button(
                onClick = { /* Finalizacja */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ZATWIERDŹ REZERWACJĘ", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun ReservationDetail(label: String, value: String) {
    Row(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(text = "$label ", color = Color.White, fontSize = 16.sp)
        Text(text = value, color = CinemaGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview
@Composable
fun ReservationPreview() {
    ReservationScreen()
}