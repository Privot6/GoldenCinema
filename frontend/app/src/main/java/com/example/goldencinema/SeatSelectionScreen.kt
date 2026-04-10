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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goldencinema.ui.theme.CinemaGold

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
    // 1. Logika wyboru miejsc (Przykładowe dane: 8 rzędów po 10 miejsc)
    val totalRows = 8
    val totalCols = 10

    // Lista miejsc w stanie "remember", żeby Compose wiedział, kiedy przerysować ekran
    val seats = remember {
        mutableStateListOf<Seat>().apply {
            for (r in 1..totalRows) {
                for (c in 1..totalCols) {
                    // Losujemy kilka zajętych miejsc dla realizmu
                    val initialStatus = if ((r+c) % 7 == 0) SeatStatus.Occupied else SeatStatus.Available
                    add(Seat(r, c, initialStatus))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Wybór Miejsc", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Dune: Part Two | 17:30", color = Color.Gray, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Tu będzie powrót do repertuaru */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cofnij",
                            tint = CinemaGold
                        )
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 2. EKRAN (Trapez na górze)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(40.dp)
                    .background(Color.DarkGray, RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("EKRAN", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 3. SIATKA MIEJSC
            Box(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(totalCols),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(seats.size) { index ->
                        val seat = seats[index]

                        // Kolor fotela zależny od statusu
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
                                    // Przełączanie statusu: Available <-> Selected
                                    seats[index] = seat.copy(
                                        status = if (seat.status == SeatStatus.Selected) SeatStatus.Available else SeatStatus.Selected
                                    )
                                }
                        )
                    }
                }
            }

            // 4. LEGENDA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem("Wolne", Color(0xFF4CAF50))
                LegendItem("Zajęte", Color(0xFFE53935))
                LegendItem("Twój wybór", Color(0xFF2196F3))
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(thickness = 1.dp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))

            // 5. INFO O WYBORZE I PRZYCISK
            val selectedSeats = seats.filter { it.status == SeatStatus.Selected }
            Text(
                text = if (selectedSeats.isEmpty()) "Wybierz miejsce"
                else "Rząd ${selectedSeats.first().row}, Miejsca ${selectedSeats.joinToString { it.column.toString() }}",
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Dalej do podsumowania */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("DALEJ", color = Color.Black, fontWeight = FontWeight.Bold)
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