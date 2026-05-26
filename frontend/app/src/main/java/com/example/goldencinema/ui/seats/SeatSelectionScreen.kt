package com.example.goldencinema

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.goldencinema.ui.theme.CinemaGold
import com.example.goldencinema.ui.theme.DarkBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    screeningId: Long,
    basePrice: Double,
    navController: NavController,
    viewModel: SeatSelectionViewModel = viewModel(
        factory = SeatSelectionViewModelFactory(screeningId, basePrice)
    )
) {
    val seatsState by viewModel.seatsState.collectAsState()
    val selectedSeatIds by viewModel.selectedSeatIds.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val reservationState by viewModel.reservationState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(reservationState) {
        when (val state = reservationState) {
            is ReservationUiState.Conflict -> {
                viewModel.loadSeats()
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetReservationState()
            }
            is ReservationUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetReservationState()
            }
            else -> {}
        }
    }

    if (reservationState is ReservationUiState.Success) {
        val code = (reservationState as ReservationUiState.Success).code
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF1E1E1E),
            title = {
                Text(
                    stringResource(R.string.reservation_confirmed),
                    color = CinemaGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "${stringResource(R.string.reservation_code_label)} $code",
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetReservationState()
                        navController.navigate("my-reservations") {
                            popUpTo("repertuar") { inclusive = false }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
                ) {
                    Text(stringResource(R.string.ok_button), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.seat_selection_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cofnij", tint = CinemaGold)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.DarkGray, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.seat_screen),
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val horizontalScrollState = rememberScrollState()

            when (val state = seatsState) {
                is SeatsUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CinemaGold)
                    }
                }
                is SeatsUiState.Error -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = Color.Red, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadSeats() },
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
                            ) {
                                Text(stringResource(R.string.retry_button), color = Color.Black)
                            }
                        }
                    }
                }
                is SeatsUiState.Success -> {
                    val allSeats = state.rows.flatMap { it.seats }
                    val maxCol = allSeats.mapNotNull { it.gridCol }.maxOrNull()

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(state.rows) { seatRow ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.horizontalScroll(horizontalScrollState)
                            ) {
                                Text(
                                    text = seatRow.rowLabel,
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (maxCol != null) {
                                    val colMap = seatRow.seats.associateBy { it.gridCol }
                                    for (col in 1..maxCol) {
                                        val seat = colMap[col]
                                        if (seat != null) {
                                            SeatBox(
                                                seat = seat,
                                                isSelected = seat.id in selectedSeatIds,
                                                onClick = { viewModel.toggleSeat(seat) }
                                            )
                                        } else {
                                            Box(modifier = Modifier.size(28.dp))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                } else {
                                    seatRow.seats.forEach { seat ->
                                        SeatBox(
                                            seat = seat,
                                            isSelected = seat.id in selectedSeatIds,
                                            onClick = { viewModel.toggleSeat(seat) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = Color(0xFF4CAF50), label = stringResource(R.string.seat_free))
                LegendItem(color = Color(0xFFE53935), label = stringResource(R.string.seat_taken))
                LegendItem(color = Color(0xFF2196F3), label = stringResource(R.string.seat_selected))
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))

            val selectedCount = selectedSeatIds.size
            val isReserving = reservationState is ReservationUiState.Loading

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${stringResource(R.string.ticket_count)} $selectedCount",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${stringResource(R.string.total_price)} ${"%.2f".format(totalPrice)} PLN",
                        color = CinemaGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { viewModel.reserve() },
                    enabled = selectedCount > 0 && !isReserving,
                    modifier = Modifier.height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        disabledContainerColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isReserving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.reserve_button), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SeatBox(seat: SeatDto, isSelected: Boolean, onClick: () -> Unit) {
    val color = when {
        !seat.isAvailable -> Color(0xFFE53935)
        isSelected -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(color, RoundedCornerShape(4.dp))
            .clickable(enabled = seat.isAvailable) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "${seat.seatNumber}", color = Color.White.copy(alpha = 0.8f), fontSize = 8.sp)
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
