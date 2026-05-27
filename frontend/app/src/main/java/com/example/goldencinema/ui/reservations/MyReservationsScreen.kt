package com.example.goldencinema

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.goldencinema.ui.theme.CinemaGold
import com.example.goldencinema.ui.theme.DarkBackground
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

private fun formatDateTime(iso: String): String {
    if (iso.length < 16) return iso
    val datePart = iso.substring(0, 10)
    val timePart = iso.substring(11, 16)
    val parts = datePart.split("-")
    return if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]} $timePart" else "$datePart $timePart"
}

private fun statusColor(status: String): Color = when (status) {
    "POTWIERDZONA" -> Color(0xFF4CAF50)
    "OCZEKUJACA"   -> Color(0xFFFFC107)
    else           -> Color(0xFF757575)
}

private fun statusTextColor(status: String): Color = when (status) {
    "OCZEKUJACA" -> Color.Black
    else         -> Color.White
}

private fun buildQrContent(reservation: ReservationResponseDto): String {
    val seats = reservation.reservedSeatsDto.joinToString(", ") { "${it.rowLabel}${it.seatNumber}" }
    val ticketCount = reservation.reservedSeatsDto.size
    val statusLabel = when (reservation.status) {
        "POTWIERDZONA" -> "OPLACONA"
        "OCZEKUJACA"   -> "OCZEKUJE NA PLATNOSC"
        "ANULOWANA"    -> "ANULOWANA"
        "WYGASLA"      -> "WYGASLA"
        else           -> reservation.status
    }
    return buildString {
        appendLine("GOLDEN CINEMA")
        appendLine("Kod: ${reservation.reservationCode}")
        appendLine("Film: ${reservation.screeningDto.movie.title}")
        appendLine("Data: ${formatDateTime(reservation.screeningDto.startTime)}")
        appendLine("Sala: ${reservation.screeningDto.hall.name}")
        appendLine("Miejsca: $seats")
        appendLine("Bilety: $ticketCount")
        appendLine("Status: $statusLabel")
        append("Kwota: ${"%.2f".format(reservation.totalPrice)} PLN")
    }
}

private fun generateQrBitmap(code: String): androidx.compose.ui.graphics.ImageBitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val bitMatrix = QRCodeWriter().encode(code, BarcodeFormat.QR_CODE, 512, 512, hints)
    val bmp = android.graphics.Bitmap.createBitmap(512, 512, android.graphics.Bitmap.Config.ARGB_8888)
    for (x in 0 until 512) {
        for (y in 0 until 512) {
            bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bmp.asImageBitmap()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(
    navController: NavController,
    viewModel: ReservationsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.my_reservations_title), color = Color.White, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = { MainBottomBar(navController, "my-reservations") },
        containerColor = DarkBackground
    ) { padding ->
        when (val s = state) {
            is ReservationsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CinemaGold)
                }
            }
            is ReservationsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.message, color = Color.Red, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadMyReservations() },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
                        ) {
                            Text(stringResource(R.string.retry_button), color = Color.Black)
                        }
                    }
                }
            }
            is ReservationsUiState.Success -> {
                PullToRefreshBox(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    if (s.reservations.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.my_reservations_empty), color = Color.Gray, fontSize = 16.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(s.reservations) { reservation ->
                                ReservationTicket(reservation)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationTicket(reservation: ReservationResponseDto) {
    var isExpanded by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }

    val seats = reservation.reservedSeatsDto
        .joinToString(", ") { "${it.rowLabel}${it.seatNumber}" }
    val movie = reservation.screeningDto.movie
    val canShowQr = reservation.status == "OCZEKUJACA" || reservation.status == "POTWIERDZONA"

    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = {
                Text("Twój bilet QR", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val qrContent = buildQrContent(reservation)
                    val qrBitmap = remember(reservation.id, reservation.status) {
                        generateQrBitmap(qrContent)
                    }
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "Kod QR rezerwacji",
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = reservation.reservationCode,
                        color = CinemaGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF333333))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = reservation.screeningDto.movie.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatDateTime(reservation.screeningDto.startTime),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sala", color = Color.Gray, fontSize = 11.sp)
                            Text(reservation.screeningDto.hall.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Bilety", color = Color.Gray, fontSize = 11.sp)
                            Text("${reservation.reservedSeatsDto.size}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Kwota", color = Color.Gray, fontSize = 11.sp)
                            Text("${"%.2f".format(reservation.totalPrice)} PLN", color = CinemaGold, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Miejsca: ${reservation.reservedSeatsDto.joinToString(", ") { "${it.rowLabel}${it.seatNumber}" }}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = statusColor(reservation.status),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = reservation.status,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = statusTextColor(reservation.status),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.ticket_qr_help),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text(stringResource(R.string.ok_button), color = CinemaGold)
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        border = if (isExpanded) BorderStroke(1.dp, CinemaGold) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                if (!movie.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(width = 56.dp, height = 80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 80.dp)
                            .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = movie.genre.take(3).uppercase(),
                            color = CinemaGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movie.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatDateTime(reservation.screeningDto.startTime),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reservation.reservationCode,
                            color = CinemaGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            color = statusColor(reservation.status),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = reservation.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = statusTextColor(reservation.status),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.ticket_seat_label), color = Color.Gray, fontSize = 12.sp)
                            Text(seats.ifEmpty { "—" }, color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.ticket_hall_label), color = Color.Gray, fontSize = 12.sp)
                            Text(reservation.screeningDto.hall.name, color = Color.White, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Łącznie", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                text = "${"%.2f".format(reservation.totalPrice)} PLN",
                                color = CinemaGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (canShowQr) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showQrDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Pokaż QR", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
