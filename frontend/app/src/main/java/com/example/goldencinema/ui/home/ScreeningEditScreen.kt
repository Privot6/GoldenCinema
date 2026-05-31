package com.example.goldencinema

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.goldencinema.ui.theme.CinemaGold
import com.example.goldencinema.ui.theme.DarkBackground
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreeningEditScreen(
    screeningId: Long,
    navController: NavController,
    viewModel: ScreeningEditViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var basePrice by remember { mutableStateOf("") }

    LaunchedEffect(screeningId) {
        viewModel.loadScreening(screeningId)
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is ScreeningEditUiState.Loaded -> {
                if (startDate.isEmpty()) {
                    startDate = s.screening.startTime.take(10)
                    startTime = if (s.screening.startTime.length >= 16) s.screening.startTime.substring(11, 16) else ""
                    endDate = s.screening.endTime.take(10)
                    endTime = if (s.screening.endTime.length >= 16) s.screening.endTime.substring(11, 16) else ""
                    basePrice = "%.2f".format(s.screening.basePrice)
                }
            }
            is ScreeningEditUiState.Saved -> {
                navController.popBackStack()
            }
            is ScreeningEditUiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                if (s.message.contains("Błąd zapisu")) {
                    viewModel.loadScreening(screeningId)
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edytuj seans", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć", tint = CinemaGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = { MainBottomBar(navController, "repertuar") },
        containerColor = DarkBackground
    ) { padding ->
        when (val s = state) {
            is ScreeningEditUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CinemaGold)
                }
            }
            is ScreeningEditUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.message, color = Color.Red, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadScreening(screeningId) },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
                        ) {
                            Text("Spróbuj ponownie", color = Color.Black)
                        }
                    }
                }
            }
            is ScreeningEditUiState.Loaded, is ScreeningEditUiState.Saving, is ScreeningEditUiState.Saved -> {
                val screening = (state as? ScreeningEditUiState.Loaded)?.screening
                val isSaving = state is ScreeningEditUiState.Saving

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Info o filmie i sali (tylko do odczytu)
                    if (screening != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Film", color = Color.Gray, fontSize = 12.sp)
                                Text(screening.movie.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sala", color = Color.Gray, fontSize = 12.sp)
                                Text(screening.hall.name, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }

                    // Czas rozpoczęcia
                    Text("Czas rozpoczęcia", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DateChip(
                            label = startDate.ifEmpty { "Data" },
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val cal = calendarFromDateString(startDate)
                                DatePickerDialog(context, { _, y, m, d ->
                                    startDate = "%04d-%02d-%02d".format(y, m + 1, d)
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                            }
                        )
                        DateChip(
                            label = startTime.ifEmpty { "Godzina" },
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val (h, m) = timeFromString(startTime)
                                TimePickerDialog(context, { _, hour, minute ->
                                    startTime = "%02d:%02d".format(hour, minute)
                                }, h, m, true).show()
                            }
                        )
                    }

                    // Czas zakończenia
                    Text("Czas zakończenia", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DateChip(
                            label = endDate.ifEmpty { "Data" },
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val cal = calendarFromDateString(endDate)
                                DatePickerDialog(context, { _, y, m, d ->
                                    endDate = "%04d-%02d-%02d".format(y, m + 1, d)
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                            }
                        )
                        DateChip(
                            label = endTime.ifEmpty { "Godzina" },
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val (h, m) = timeFromString(endTime)
                                TimePickerDialog(context, { _, hour, minute ->
                                    endTime = "%02d:%02d".format(hour, minute)
                                }, h, m, true).show()
                            }
                        )
                    }

                    // Cena bazowa
                    OutlinedTextField(
                        value = basePrice,
                        onValueChange = { basePrice = it },
                        label = { Text("Cena bazowa (PLN)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val movieId = screening?.movie?.id ?: return@Button
                            val hallId = screening.hall.id
                            val startIso = "${startDate}T${startTime}:00"
                            val endIso = "${endDate}T${endTime}:00"
                            viewModel.save(screeningId, movieId, hallId, startIso, endIso, basePrice)
                        },
                        enabled = !isSaving && startDate.isNotEmpty() && startTime.isNotEmpty()
                                && endDate.isNotEmpty() && endTime.isNotEmpty() && basePrice.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Text("Zapisz zmiany", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E1E1E),
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = CinemaGold, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

private fun calendarFromDateString(date: String): Calendar {
    val cal = Calendar.getInstance()
    if (date.length == 10) {
        runCatching {
            val parts = date.split("-")
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }
    }
    return cal
}

private fun timeFromString(time: String): Pair<Int, Int> {
    if (time.length >= 5) {
        runCatching {
            val parts = time.split(":")
            return Pair(parts[0].toInt(), parts[1].toInt())
        }
    }
    return Pair(12, 0)
}
