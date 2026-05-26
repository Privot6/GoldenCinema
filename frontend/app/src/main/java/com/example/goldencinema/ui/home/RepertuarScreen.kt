package com.example.goldencinema

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.goldencinema.ui.theme.CinemaGold
import java.time.LocalDate

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}min"
        h > 0 -> "${h}h"
        else -> "${m}min"
    }
}

private fun formatDateChip(isoDate: String): String {
    return try {
        val date = LocalDate.parse(isoDate)
        val days = arrayOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")
        val dow = days[date.dayOfWeek.value - 1]
        val d = date.dayOfMonth.toString().padStart(2, '0')
        val mo = date.monthValue.toString().padStart(2, '0')
        "$dow $d.$mo"
    } catch (e: Exception) {
        isoDate
    }
}

private fun extractDate(isoDateTime: String) = isoDateTime.take(10)

private fun extractTime(isoDateTime: String) =
    if (isoDateTime.length >= 16) isoDateTime.substring(11, 16) else isoDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepertuarScreen(
    navController: NavController,
    viewModel: ScreeningViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.repertuar_title), color = Color.White, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = { MainBottomBar(navController, "repertuar") },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Szukaj filmu…", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Wyczyść", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CinemaGold,
                    unfocusedBorderColor = Color(0xFF2A2A2A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = CinemaGold,
                    unfocusedContainerColor = Color(0xFF1A1A1A),
                    focusedContainerColor = Color(0xFF1A1A1A)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )

        when (val s = state) {
            is ScreeningsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CinemaGold)
                }
            }
            is ScreeningsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.message, color = Color.Red, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadScreenings() },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
                        ) {
                            Text(stringResource(R.string.retry_button), color = Color.Black)
                        }
                    }
                }
            }
            is ScreeningsUiState.Success -> {
                if (s.screenings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Brak nadchodzących seansów", color = Color.Gray)
                    }
                } else {
                    val movieGroups = remember(s.screenings) {
                        s.screenings.groupBy { it.movie.id }
                    }
                    val allMovies = remember(movieGroups) {
                        movieGroups.keys.mapNotNull { id ->
                            s.screenings.find { it.movie.id == id }?.movie
                        }
                    }
                    val movies = remember(allMovies, query) {
                        if (query.length < 2) allMovies
                        else allMovies.filter { it.title.contains(query.trim(), ignoreCase = true) }
                    }

                    var expandedMovieId by remember { mutableStateOf<Long?>(null) }
                    val selectedDates = remember { mutableStateMapOf<Long, String>() }

                    if (movies.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Brak wyników dla \"$query\"", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(movies, key = { it.id }) { movie ->
                            val screenings = movieGroups[movie.id] ?: emptyList()
                            val isExpanded = expandedMovieId == movie.id
                            val selectedDate = selectedDates[movie.id]
                                ?: screenings.map { extractDate(it.startTime) }.distinct().sorted().firstOrNull()

                            MovieCard(
                                movie = movie,
                                screenings = screenings,
                                isExpanded = isExpanded,
                                selectedDate = selectedDate,
                                onHeaderClick = {
                                    expandedMovieId = if (isExpanded) null else movie.id
                                },
                                onDateSelected = { date -> selectedDates[movie.id] = date },
                                onScreeningSelected = { screening ->
                                    navController.navigate(
                                        "seats/${screening.id}/${String.format(java.util.Locale.US, "%.2f", screening.basePrice)}"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
}

@Composable
fun MovieCard(
    movie: MovieDto,
    screenings: List<ScreeningDto>,
    isExpanded: Boolean,
    selectedDate: String?,
    onHeaderClick: () -> Unit,
    onDateSelected: (String) -> Unit,
    onScreeningSelected: (ScreeningDto) -> Unit
) {
    val dates = remember(screenings) {
        screenings.map { extractDate(it.startTime) }.distinct().sorted()
    }
    val screeningsForDate = remember(screenings, selectedDate) {
        if (selectedDate == null) emptyList()
        else screenings.filter { extractDate(it.startTime) == selectedDate }.sortedBy { it.startTime }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Nagłówek (zawsze widoczny)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!movie.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(width = 70.dp, height = 100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(width = 70.dp, height = 100.dp)
                            .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = movie.genre.take(3).uppercase(),
                            color = CinemaGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(movie.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${movie.genre}  •  ${formatDuration(movie.durationMinutes)}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${screenings.size} ${if (screenings.size == 1) "seans" else "seansów"}",
                        color = CinemaGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Rozwinięta sekcja
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(color = Color(0xFF2A2A2A))

                    // Chipsy z datami
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dates) { date ->
                            val isSelected = date == selectedDate
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) CinemaGold else Color(0xFF2A2A2A),
                                modifier = Modifier.clickable { onDateSelected(date) }
                            ) {
                                Text(
                                    text = formatDateChip(date),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = if (isSelected) Color.Black else Color.LightGray,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Seanse dla wybranej daty
                    if (screeningsForDate.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Brak seansów w tym dniu", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 12.dp)
                        ) {
                            screeningsForDate.forEachIndexed { index, screening ->
                                ScreeningRow(
                                    screening = screening,
                                    onClick = { onScreeningSelected(screening) }
                                )
                                if (index < screeningsForDate.lastIndex) {
                                    HorizontalDivider(color = Color(0xFF2A2A2A))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreeningRow(screening: ScreeningDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = CinemaGold,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = extractTime(screening.startTime),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = screening.hall.name,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${"%.2f".format(screening.basePrice)} PLN",
            color = CinemaGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.width(10.dp))

        Button(
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(34.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
        ) {
            Text(
                stringResource(R.string.buy_ticket_button),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
