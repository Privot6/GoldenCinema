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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import com.example.goldencinema.R
import com.example.goldencinema.ui.theme.CinemaGold
import com.example.goldencinema.ui.theme.DarkBackground

// 1. MODEL DANYCH
data class Movie(
    val title: String,
    val genre: String,
    val times: List<String>
)

@Composable
fun DateItem(dayName: String, dayNumber: String, isSelected: Boolean) {
    val bgColor = if (isSelected) CinemaGold else Color(0xFF1E1E1E)
    val textColor = if (isSelected) Color.Black else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(text = dayName, color = textColor, fontSize = 12.sp)
        Text(text = dayNumber, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

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

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* TODO: Przejście do wyboru miejsc */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
                ) {
                    Text(stringResource(R.string.buy_ticket_button), color = Color.Black, fontWeight = FontWeight.Bold)
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
                title = { Text(stringResource(R.string.repertuar_title), color = Color.White, fontWeight = FontWeight.Bold) },
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
            // Nagłówek (Menu i Tytuł)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Menu */ }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = CinemaGold)
                }
                Text(
                    text = stringResource(R.string.repertuar_title),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { /* Ulubione */ }) {
                    Icon(Icons.Default.Favorite, contentDescription = "Ulubione", tint = CinemaGold)
                }
            }

            // Wybór Daty (Oś czasu)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DateItem(stringResource(R.string.day_today), "15", true)
                DateItem(stringResource(R.string.day_tomorrow), "16", false)
                DateItem("Śro", "17", false)
                DateItem("Czw", "18", false)
                DateItem("Pią", "19", false)
                DateItem("Sob", "20", false)
            }

            Spacer(modifier = Modifier.height(24.dp))

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