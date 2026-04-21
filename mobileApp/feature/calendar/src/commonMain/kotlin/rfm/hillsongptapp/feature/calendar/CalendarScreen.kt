package rfm.hillsongptapp.feature.calendar

import AppFonts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.core.designsystem.ui.components.EditorialSectionHeader
import rfm.hillsongptapp.core.network.api.CalendarEvent

@Composable
fun CalendarScreen(
    navController: NavHostController,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "CALENDAR",
                    style = TextStyle(
                        fontFamily = AppFonts.anta(),
                        fontSize = 14.sp,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HillsongColors.Gold)
                }
            } else {
                // Month header with arrows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { viewModel.onEvent(CalendarUiEvent.PreviousMonth) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous month",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Text(
                        text = "${monthName(uiState.currentMonth)} ${uiState.currentYear}",
                        style = TextStyle(
                            fontFamily = AppFonts.anta(),
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.3.sp,
                        ),
                    )
                    IconButton(onClick = { viewModel.onEvent(CalendarUiEvent.NextMonth) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next month",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                // Day of week labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
                        Text(
                            text = d,
                            style = TextStyle(
                                fontFamily = AppFonts.andika(),
                                fontSize = 10.sp,
                                color = HillsongColors.Gray500,
                                letterSpacing = 1.sp,
                            ),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Calendar grid
                CalendarGrid(
                    month = uiState.currentMonth,
                    year = uiState.currentYear,
                    events = uiState.events,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { viewModel.onEvent(CalendarUiEvent.SelectDate(it)) },
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )

                // Events section
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    if (uiState.selectedDate == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 40.dp),
                            ) {
                                EditorialSectionHeader(
                                    title = "Upcoming",
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                )
                                Spacer(Modifier.height(24.dp))
                                Text(
                                    text = "Select a date to view events",
                                    style = TextStyle(
                                        fontFamily = AppFonts.andika(),
                                        fontSize = 13.sp,
                                        color = HillsongColors.Gray500,
                                    ),
                                )
                            }
                        }
                    } else {
                        Column {
                            EditorialSectionHeader(
                                title = formatDisplayDate(uiState.selectedDate!!),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                            )
                            Spacer(Modifier.height(12.dp))
                            if (uiState.selectedDayEvents.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(top = 32.dp),
                                    contentAlignment = Alignment.TopCenter,
                                ) {
                                    Text(
                                        text = "No events on this day.",
                                        style = TextStyle(
                                            fontFamily = AppFonts.andika(),
                                            fontSize = 13.sp,
                                            color = HillsongColors.Gray500,
                                        ),
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    items(uiState.selectedDayEvents) { event ->
                                        EditorialEventRow(event = event)
                                    }
                                    item { Spacer(Modifier.height(40.dp)) }
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
private fun CalendarGrid(
    month: Int,
    year: Int,
    events: Map<String, List<CalendarEvent>>,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val firstDayOfMonth = LocalDate(year, month, 1)
    val daysInMonth = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> 30
    }
    val startOffset = when (firstDayOfMonth.dayOfWeek) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }
    val prevMonth = if (month == 1) 12 else month - 1
    val prevYear = if (month == 1) year - 1 else year
    val daysInPrevMonth = when (prevMonth) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (prevYear % 4 == 0 && (prevYear % 100 != 0 || prevYear % 400 == 0)) 29 else 28
        else -> 30
    }
    val nextMonth = if (month == 12) 1 else month + 1
    val nextYear = if (month == 12) year + 1 else year

    val cells = mutableListOf<DayCell>()
    for (i in startOffset - 1 downTo 0) {
        val d = daysInPrevMonth - i
        cells.add(DayCell(d, formatDate(prevYear, prevMonth, d), isCurrentMonth = false, hasEvents = events.containsKey(formatDate(prevYear, prevMonth, d))))
    }
    for (d in 1..daysInMonth) {
        val date = formatDate(year, month, d)
        cells.add(DayCell(d, date, isCurrentMonth = true, hasEvents = events.containsKey(date)))
    }
    var nextDay = 1
    while (cells.size < 42) {
        val date = formatDate(nextYear, nextMonth, nextDay)
        cells.add(DayCell(nextDay, date, isCurrentMonth = false, hasEvents = events.containsKey(date)))
        nextDay++
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        cells.chunked(7).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { cell ->
                    Box(modifier = Modifier.weight(1f)) {
                        CalendarDayCell(
                            cell = cell,
                            isToday = cell.date == formatDate(today.year, today.monthNumber, today.dayOfMonth),
                            isSelected = cell.date == selectedDate,
                            onClick = { onDateSelected(cell.date) },
                        )
                    }
                }
            }
        }
    }
}

private data class DayCell(val day: Int, val date: String, val isCurrentMonth: Boolean, val hasEvents: Boolean)

@Composable
private fun CalendarDayCell(cell: DayCell, isToday: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Circle background
        if (isSelected) {
            Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(HillsongColors.Gold))
        } else if (isToday) {
            Box(
                modifier = Modifier.size(30.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
            )
            Box(
                modifier = Modifier.size(30.dp).clip(CircleShape)
                    .background(HillsongColors.Gold.copy(alpha = 0f)),
            )
            // Gold border for today — use a separate border approach
            androidx.compose.foundation.Canvas(modifier = Modifier.size(30.dp)) {
                drawCircle(color = HillsongColors.Gold, radius = size.minDimension / 2f - 1.5f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = cell.day.toString(),
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> HillsongColors.Black
                        cell.isCurrentMonth -> MaterialTheme.colorScheme.onBackground
                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    },
                ),
            )
            if (cell.hasEvents && cell.isCurrentMonth && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(HillsongColors.Gold),
                )
            }
        }
    }
}

@Composable
private fun EditorialEventRow(event: CalendarEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (event.startTime != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(HillsongColors.Gold.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = buildString {
                            append(event.startTime)
                            event.endTime?.let { append(" – $it") }
                        },
                        style = TextStyle(
                            fontFamily = AppFonts.andika(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = HillsongColors.Gold,
                            letterSpacing = 0.5.sp,
                        ),
                    )
                }
                Spacer(Modifier.height(8.dp))
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
            event.location?.let { loc ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = loc,
                    style = TextStyle(
                        fontFamily = AppFonts.andika(),
                        fontSize = 12.sp,
                        color = HillsongColors.Gray500,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

private fun monthName(month: Int) = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)[month - 1]

private fun formatDate(year: Int, month: Int, day: Int) =
    "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

private fun formatDisplayDate(date: String): String {
    if (date.length < 10) return date
    val parts = date.split("-")
    if (parts.size != 3) return date
    val month = parts[1].toIntOrNull() ?: return date
    val day = parts[2].toIntOrNull() ?: return date
    return "${monthName(month)} $day"
}
