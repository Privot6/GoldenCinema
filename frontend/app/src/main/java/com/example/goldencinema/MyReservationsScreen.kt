package com.example.goldencinema

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
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

// 1. MODEL DANYCH REZERWACJI
data class Reservation(
    val id: String,
    val movieTitle: String,
    val date: String,
    val time: String,
    val seats: String,
    val hall: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen() {
    // Przykładowe dane
    val reservations = listOf(
        Reservation("1", "Diuna: Część Druga", "15 Maj 2024", "18:00", "Rząd 4, Miejsce 12, 13", "Potwierdzona"),
        Reservation("2", "Deadpool & Wolverine", "20 Maj 2024", "20:30", "Rząd 8, Miejsce 15", "Oczekująca")
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nagłówek i przycisk powrotu
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Powrót do profilu/menu */ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = CinemaGold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Tytuł ekranu
                Text(
                    text = stringResource(R.string.my_reservations_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            // Główna zawartość
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Jeśli brak rezerwacji
                if (reservations.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.my_reservations_empty),
                            color = Color.Gray,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(reservations) { reservation ->
                        ReservationTicket(reservation)
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationTicket(reservation: Reservation) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        border = if (isExpanded) BorderStroke(1.dp, CinemaGold) else null
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // NAGŁÓWEK BILETU (Zawsze widoczny)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = reservation.movieTitle, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = reservation.date, color = Color.Gray, fontSize = 14.sp)
                }
                Text(text = reservation.time, color = CinemaGold, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }

            // SZCZEGÓŁY (Widoczne po kliknięciu)
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.ticket_seat_label), color = Color.Gray, fontSize = 12.sp)
                            Text(reservation.seats, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(stringResource(R.string.ticket_hall_label), color = Color.Gray, fontSize = 12.sp)
                            Text(reservation.hall, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        // PLACEHOLDER NA KOD QR
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.White, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "Kod QR",
                                modifier = Modifier.size(60.dp),
                                tint = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.ticket_qr_help),
                        color = CinemaGold,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun MyReservationsPreview() {
    MyReservationsScreen()
}
