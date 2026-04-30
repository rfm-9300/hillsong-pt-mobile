package rfm.hillsongptapp.feature.events

import AppFonts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.core.navigation.navigateToEventQrCheckIn
import rfm.hillsongptapp.core.navigation.navigateToMyQr

@Composable
fun EventDetailScreen(
    eventId: String,
    navController: NavHostController,
    viewModel: EventDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(eventId) { viewModel.load(eventId) }

    LaunchedEffect(uiState.successMessage, uiState.error) {
        val msg = uiState.successMessage ?: uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearMessages()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) } }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "EVENT",
                    style = TextStyle(
                        fontFamily = AppFonts.anta(),
                        fontSize = 14.sp,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HillsongColors.Gold)
                    }
                }
                uiState.error != null && uiState.event == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Could not load event.",
                            style = TextStyle(
                                fontFamily = AppFonts.andika(),
                                fontSize = 13.sp,
                                color = HillsongColors.Gray500
                            ),
                        )
                    }
                }
                uiState.event != null -> {
                    val event = uiState.event!!
                    val status = uiState.status

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Hero image
                        val imageUrl = event.headerImagePath?.let { "${viewModel.baseUrl}/api/files/$it" }
                        if (imageUrl != null) {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 7f)) {
                                rfm.hillsongptapp.util.media.AsyncImage(
                                    imageUrl = imageUrl,
                                    contentDescription = event.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    onFailure = {},
                                )
                                Box(
                                    modifier = Modifier.fillMaxSize().background(
                                        Brush.verticalGradient(
                                            colorStops = arrayOf(
                                                0.5f to Color.Transparent,
                                                1.0f to Color.Black.copy(alpha = 0.6f),
                                            ),
                                        ),
                                    ),
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                            // Date + time
                            val (dayNum, monthShort) = formatEventDateBlock(event.date)
                            val time = formatEventTime(event.date)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = dayNum,
                                        style = TextStyle(
                                            fontFamily = AppFonts.mogra(),
                                            fontSize = 22.sp,
                                            color = HillsongColors.Gold,
                                            lineHeight = 24.sp,
                                        ),
                                    )
                                    Text(
                                        text = monthShort,
                                        style = TextStyle(
                                            fontFamily = AppFonts.anta(),
                                            fontSize = 10.sp,
                                            letterSpacing = 1.sp,
                                            color = HillsongColors.Gray500,
                                        ),
                                    )
                                }

                                Column {
                                    if (time.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(HillsongColors.Gold.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp),
                                        ) {
                                            Text(
                                                text = time,
                                                style = TextStyle(
                                                    fontFamily = AppFonts.andika(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = HillsongColors.Gold,
                                                    letterSpacing = 0.5.sp,
                                                ),
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }
                                    Text(
                                        text = event.location.uppercase(),
                                        style = TextStyle(
                                            fontFamily = AppFonts.andika(),
                                            fontSize = 12.sp,
                                            color = HillsongColors.Gray500,
                                            letterSpacing = 0.5.sp,
                                        ),
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Title
                            Text(
                                text = event.title,
                                style = TextStyle(
                                    fontFamily = AppFonts.andika(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    lineHeight = 28.sp,
                                ),
                            )

                            Spacer(Modifier.height(8.dp))

                            // Organizer
                            Text(
                                text = "Organizado por ${event.organizerName}",
                                style = TextStyle(
                                    fontFamily = AppFonts.andika(),
                                    fontSize = 12.sp,
                                    color = HillsongColors.Gray500,
                                ),
                            )

                            Spacer(Modifier.height(16.dp))

                            // Attendee progress
                            if (event.maxAttendees > 0) {
                                val percent = (event.attendeeCount.toFloat() / event.maxAttendees).coerceIn(0f, 1f)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "${event.attendeeCount}/${event.maxAttendees} REGISTADOS",
                                        style = TextStyle(
                                            fontFamily = AppFonts.anta(),
                                            fontSize = 10.sp,
                                            color = if (event.isAtCapacity) HillsongColors.Error else HillsongColors.Gray500,
                                            letterSpacing = 0.8.sp,
                                        ),
                                    )
                                    if (event.availableSpots > 0) {
                                        Text(
                                            text = "${event.availableSpots} disponíveis",
                                            style = TextStyle(
                                                fontFamily = AppFonts.anta(),
                                                fontSize = 10.sp,
                                                color = HillsongColors.Gray500,
                                                letterSpacing = 0.8.sp,
                                            ),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { percent },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = if (event.isAtCapacity) HillsongColors.Error else HillsongColors.Gold,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            // Description
                            Text(
                                text = event.description,
                                style = TextStyle(
                                    fontFamily = AppFonts.andika(),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    lineHeight = 22.sp,
                                ),
                            )

                            Spacer(Modifier.height(28.dp))

                            // CTA buttons
                            val currentStatus = status?.status ?: "NOT_JOINED"
                            val canJoin = status?.canJoin ?: (!event.isAtCapacity)
                            val canLeave = status?.canLeave ?: false

                            if (uiState.canScanQr) {
                                OutlinedButton(
                                    onClick = { navController.navigateToEventQrCheckIn(eventId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Text(
                                        text = "Ler QR para check-in",
                                        style = TextStyle(
                                            fontFamily = AppFonts.andika(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                        ),
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                            }

                            when (currentStatus) {
                                "ATTENDEE" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = { navController.navigateToMyQr() },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = HillsongColors.Gold),
                                            shape = RoundedCornerShape(10.dp),
                                        ) {
                                            Text(
                                                text = "Ver o meu QR de entrada",
                                                style = TextStyle(
                                                    fontFamily = AppFonts.andika(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                ),
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.leaveEvent(eventId) },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !uiState.isActing,
                                            shape = RoundedCornerShape(10.dp),
                                        ) {
                                            if (uiState.isActing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = HillsongColors.Gold,
                                                    strokeWidth = 2.dp,
                                                )
                                            } else {
                                                Text(
                                                    text = "Cancelar inscrição",
                                                    style = TextStyle(
                                                        fontFamily = AppFonts.andika(),
                                                        fontSize = 14.sp,
                                                    ),
                                                )
                                            }
                                        }
                                    }
                                }
                                "WAITING_LIST", "PENDING_APPROVAL" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = if (currentStatus == "PENDING_APPROVAL") "Aguardando aprovação" else "Na lista de espera",
                                            style = TextStyle(
                                                fontFamily = AppFonts.andika(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = HillsongColors.Gold,
                                                textAlign = TextAlign.Center,
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                        if (canLeave) {
                                            OutlinedButton(
                                                onClick = { viewModel.leaveEvent(eventId) },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = !uiState.isActing,
                                                shape = RoundedCornerShape(10.dp),
                                            ) {
                                                Text(
                                                    text = "Sair da lista de espera",
                                                    style = TextStyle(fontFamily = AppFonts.andika(), fontSize = 14.sp),
                                                )
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    if (event.isAtCapacity) {
                                        Button(
                                            onClick = {},
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = false,
                                            shape = RoundedCornerShape(10.dp),
                                        ) {
                                            Text(
                                                text = "Evento esgotado",
                                                style = TextStyle(fontFamily = AppFonts.andika(), fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.joinEvent(eventId) },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = canJoin && !uiState.isActing,
                                            colors = ButtonDefaults.buttonColors(containerColor = HillsongColors.Gold),
                                            shape = RoundedCornerShape(10.dp),
                                        ) {
                                            if (uiState.isActing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp,
                                                )
                                            } else {
                                                Text(
                                                    text = if (event.needsApproval) "Pedir inscrição" else "Inscrever-me",
                                                    style = TextStyle(
                                                        fontFamily = AppFonts.andika(),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                    ),
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}
