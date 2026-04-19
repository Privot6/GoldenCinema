package com.example.goldencinema
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
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

enum class SeatStatus {
    Available, // Zielony
    Occupied,  // Czerwony
    Selected   // Niebieski
}

data class Seat(
    val row: Int,
    val column: Int,
    var status: SeatStatus
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen() {
    // 1. Logika wyboru miejsc (PrzykĹ‚adowe dane: 8 rzÄ™dĂłw po 10 miejsc)
    val totalRows = 8
    val totalCols = 10

    // Lista miejsc w stanie "remember", ĹĽeby Compose wiedziaĹ‚, kiedy przerysowaÄ‡ ekran
    val seats = remember {
        mutableStateListOf<Seat>().apply {
            for (r in 1..totalRows) {
                for (c in 1..totalCols) {
                    // Losujemy kilka zajÄ™tych miejsc dla realizmu
                    val initialStatus = if ((r+c) % 7 == 0) SeatStatus.Occupied else SeatStatus.Available
                    add(Seat(r, c, initialStatus))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.seat_selection_title), color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Tu bÄ™dzie powrĂłt do repertuaru */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cofnij",
                            tint = CinemaGold
                        )
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. EKRAN KINOWY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.DarkGray, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.seat_screen), color = Color.LightGray, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. SIATKA MIEJSC
            Box(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(totalCols),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(seats.size) { index ->
                        val seat = seats[index]

                        // Kolor fotela zaleĹĽny od statusu
                        val color = when (seat.status) {
                            SeatStatus.Available -> Color(0xFF4CAF50) // Zielony
                            SeatStatus.Occupied -> Color(0xFFE53935)  // Czerwony
                            SeatStatus.Selected -> Color(0xFF2196F3)  // Niebieski
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(color, RoundedCornerShape(4.dp))
                                .clickable(enabled = seat.status != SeatStatus.Occupied) {
                                    // PrzeĹ‚Ä…czanie statusu: Available <-> Selected
                                    seats[index] = seat.copy(
                                        status = if (seat.status == SeatStatus.Selected) SeatStatus.Available else SeatStatus.Selected
                                    )
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. LEGENDA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = Color.DarkGray, label = stringResource(R.string.seat_free))
                LegendItem(color = Color.Red.copy(alpha = 0.7f), label = stringResource(R.string.seat_taken))
                LegendItem(color = CinemaGold, label = stringResource(R.string.seat_selected))
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))

            // 4. PODSUMOWANIE I PRZYCISK
            val selectedCount = seats.count { it.status == SeatStatus.Selected }
            val totalPrice = selectedCount * 25 // 25 PLN za bilet (przykładowo)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "${stringResource(R.string.ticket_count)} $selectedCount", color = Color.Gray, fontSize = 14.sp)
                    Text(text = "${stringResource(R.string.total_price)} $totalPrice PLN", color = CinemaGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { /* TODO: Zapisz rezerwację */ },
                    enabled = selectedCount > 0,
                    modifier = Modifier.height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        disabledContainerColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.reserve_button), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Preview
@Composable
fun SeatPreview() {
    SeatSelectionScreen()
}
