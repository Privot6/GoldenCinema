package com.example.goldencinema

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goldencinema.ui.theme.CinemaGold

// 1. MODEL DANYCH
data class Movie(
    val title: String,
    val genre: String,
    val times: List<String>
)

// 2. POMOCNICZY ELEMENT: GODZINA (Żółty prostokąt)
@Composable
fun TimeBadge(time: String) {
    Surface(
        color = CinemaGold,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = time,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// 3. POJEDYNCZY WIERSZ FILMU
@Composable
fun MovieItem(movie: Movie) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miejsce na plakat (Szary prostokąt)
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 130.dp)
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = movie.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = movie.genre,
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    movie.times.forEach { time ->
                        TimeBadge(time)
                    }
                }
            }
        }
    }
}

// 4. GŁÓWNY EKRAN REPERTUARU
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepertuarScreen() {
    val movies = listOf(
        Movie("Dune: Part Two", "Akcja, Sci-Fi", listOf("14:00", "17:30", "20:00")),
        Movie("Kung Fu Panda 4", "Animacja, Familijny", listOf("14:00", "16:00", "18:30")),
        Movie("The Batman", "Thriller", listOf("15:00", "19:45", "22:00")),
        Movie("Oppenheimer", "Dramat, Historyczny", listOf("12:00", "16:30", "20:30"))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Repertuar", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Tutaj w przyszłości dodamy listę dat (LazyRow)
            Text(
                text = "Dzisiaj, 12 Kwi",
                color = CinemaGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(movies) { movie ->
                    MovieItem(movie)
                }
            }
        }
    }
}

// 5. PODGLĄD
@Preview
@Composable
fun RepertuarPreview() {
    RepertuarScreen()
}