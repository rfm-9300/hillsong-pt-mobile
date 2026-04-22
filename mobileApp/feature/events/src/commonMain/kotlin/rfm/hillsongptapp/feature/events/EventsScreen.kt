package rfm.hillsongptapp.feature.events

import AppFonts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.core.designsystem.ui.components.EditorialSectionHeader
import rfm.hillsongptapp.core.network.api.Event

@Composable
fun EventsScreen(
    navController: NavHostController,
    viewModel: EventsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "EVENTS",
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
                uiState.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Could not load events.",
                            style = TextStyle(fontFamily = AppFonts.andika(), fontSize = 13.sp,
                                color = HillsongColors.Gray500),
                        )
                    }
                }
                uiState.events.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No upcoming events.",
                            style = TextStyle(fontFamily = AppFonts.andika(), fontSize = 13.sp,
                                color = HillsongColors.Gray500),
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 40.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            EditorialSectionHeader(
                                title = "Upcoming",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            )
                        }
                        items(uiState.events) { event ->
                            EventCard(
                                event = event,
                                imageUrl = event.headerImagePath?.let {
                                    "${viewModel.baseUrl}/api/files/$it"
                                },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: Event, imageUrl: String?) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header image
        if (imageUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 7f),
            ) {
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Date block
            val (dayNum, monthShort) = formatEventDateBlock(event.date)
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

            Column(modifier = Modifier.weight(1f)) {
                // Time pill
                val time = formatEventTime(event.date)
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
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    text = event.title,
                    style = TextStyle(
                        fontFamily = AppFonts.andika(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 20.sp,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (event.location.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = event.location.uppercase(),
                        style = TextStyle(
                            fontFamily = AppFonts.andika(),
                            fontSize = 12.sp,
                            color = HillsongColors.Gray500,
                            letterSpacing = 0.5.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (event.maxAttendees > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${event.attendeeCount}/${event.maxAttendees} REGISTADOS",
                        style = TextStyle(
                            fontFamily = AppFonts.anta(),
                            fontSize = 10.sp,
                            color = if (event.isAtCapacity) HillsongColors.Error else HillsongColors.Gray500,
                            letterSpacing = 0.8.sp,
                        ),
                    )
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = HillsongColors.Gray500,
                modifier = Modifier.size(16.dp).padding(top = 2.dp),
            )
        }
    }
}

private fun parseEventDateTime(raw: String): kotlinx.datetime.LocalDateTime? = try {
    Instant.parse(raw).toLocalDateTime(TimeZone.of("Europe/Lisbon"))
} catch (_: Exception) {
    try { LocalDateTime.parse(raw.substringBefore(".")) } catch (_: Exception) { null }
}

private fun formatEventDateBlock(raw: String): Pair<String, String> {
    val dt = parseEventDateTime(raw) ?: return Pair("?", "?")
    val month = when (dt.monthNumber) {
        1 -> "JAN"; 2 -> "FEV"; 3 -> "MAR"; 4 -> "ABR"; 5 -> "MAI"; 6 -> "JUN"
        7 -> "JUL"; 8 -> "AGO"; 9 -> "SET"; 10 -> "OUT"; 11 -> "NOV"; 12 -> "DEZ"
        else -> ""
    }
    return Pair(dt.dayOfMonth.toString(), month)
}

private fun formatEventTime(raw: String): String {
    val dt = parseEventDateTime(raw) ?: return ""
    return "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
}
