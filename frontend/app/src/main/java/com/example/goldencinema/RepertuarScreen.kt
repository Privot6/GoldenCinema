package com.example.goldencinema

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

private fun formatTime(isoDateTime: String): String =
    if (isoDateTime.length >= 16) isoDateTime.substring(11, 16) else isoDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepertuarScreen(
    navController: NavController,
    viewModel: ScreeningViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.repertuar_title), color = Color.White, fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = {
                        TokenStore.clear()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Wyloguj",
                            tint = CinemaGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = { MainBottomBar(navController, "repertuar") },
        containerColor = Color.Black
    ) { paddingValues ->
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
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("Brak nadchodzących seansów", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(s.screenings) { screening ->
                            ScreeningItem(screening = screening, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreeningItem(screening: ScreeningDto, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            if (!screening.movie.posterUrl.isNullOrBlank()) {
                AsyncImage(
                    model = screening.movie.posterUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 90.dp, height = 130.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 130.dp)
                        .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = screening.movie.genre.take(3).uppercase(),
                        color = CinemaGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(screening.movie.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(screening.movie.genre, color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = CinemaGold, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = formatTime(screening.startTime),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(screening.hall.name, color = Color.Gray, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${"%.2f".format(screening.basePrice)} PLN",
                    color = CinemaGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        navController.navigate("seats/${screening.id}/${String.format(java.util.Locale.US, "%.2f", screening.basePrice)}")
                    },
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
