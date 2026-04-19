package com.example.goldencinema

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goldencinema.R
import com.example.goldencinema.ui.theme.CinemaGold
import com.example.goldencinema.ui.theme.DarkBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen() {
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.reservation_title), color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* PowrĂłt */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = CinemaGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            // 1. NAGŁÓWEK Z TYTUŁEM FILMU I PRZYCISKIEM POWROTU
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Przycisk powrotu
                IconButton(onClick = { /* TODO: Powrót do poprzedniego ekranu */ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = CinemaGold)
                }

                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = stringResource(R.string.reservation_title), color = Color.Gray, fontSize = 14.sp)
                    Text(text = "Diuna: Część Druga", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. WYBÓR DATY
            Text(text = stringResource(R.string.choose_date), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Przykładowy widok wyboru daty (można zastąpić własnym komponentem)
            OutlinedTextField(
                value = "2023-10-10",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Wybierz datę", color = Color.DarkGray) },
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

            Spacer(modifier = Modifier.height(24.dp))

            // 3. WYBÓR GODZINY
            Text(text = stringResource(R.string.choose_time), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Przykładowy widok wyboru godziny (można zastąpić własnym komponentem)
            OutlinedTextField(
                value = "17:30",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Wybierz godzinę", color = Color.DarkGray) },
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

            Spacer(modifier = Modifier.height(24.dp))

            // 4. PRZYCISK "DALEJ" NA SAMYM DOLE
            Button(
                onClick = { /* TODO: Przejście do wyboru miejsc */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(R.string.continue_button), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Preview
@Composable
fun ReservationPreview() {
    ReservationScreen()
}
