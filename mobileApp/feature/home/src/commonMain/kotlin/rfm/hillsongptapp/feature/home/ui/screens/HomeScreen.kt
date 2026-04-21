package rfm.hillsongptapp.feature.home.ui.screens

import AppFonts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import hillsongptapp.feature.home.generated.resources.Res
import hillsongptapp.feature.home.generated.resources.no_upcoming_encounters
import hillsongptapp.feature.home.generated.resources.no_videos_available
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.designsystem.theme.AppTheme
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.core.designsystem.ui.components.EditorialSectionHeader
import rfm.hillsongptapp.core.designsystem.ui.components.HillsongWordmark
import rfm.hillsongptapp.core.navigation.navigateToCalendar
import rfm.hillsongptapp.core.navigation.navigateToEvents
import rfm.hillsongptapp.core.navigation.navigateToFeed
import rfm.hillsongptapp.core.navigation.navigateToGiving
import rfm.hillsongptapp.core.navigation.navigateToGroups
import rfm.hillsongptapp.core.navigation.navigateToKids
import rfm.hillsongptapp.core.navigation.navigateToMinistries
import rfm.hillsongptapp.core.navigation.navigateToProfile
import rfm.hillsongptapp.core.navigation.navigateToStream
import rfm.hillsongptapp.core.navigation.navigateToYouTubeVideo
import rfm.hillsongptapp.core.network.api.Encounter
import rfm.hillsongptapp.core.network.api.YouTubeVideo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun homeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            HomeBottomNav(
                onHomeClick = { /* already on home */ },
                onEventsClick = { navController.navigateToEvents() },
                onProfileClick = { navController.navigateToProfile() },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            HomeContent(
                paddingValues = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                upcomingEncounters = uiState.upcomingEncounters,
                isLoadingEncounters = uiState.isLoadingEncounters,
                youtubeVideos = uiState.youtubeVideos,
                isLoadingVideos = uiState.isLoadingVideos,
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                onVideoClick = { video ->
                    navController.navigateToYouTubeVideo(video.id, video.videoUrl)
                },
                onExploreTile = { tile ->
                    when (tile) {
                        "feed" -> navController.navigateToFeed()
                        "calendar" -> navController.navigateToCalendar()
                        "kids" -> navController.navigateToKids()
                        "groups" -> navController.navigateToGroups()
                        "giving" -> navController.navigateToGiving()
                        "stream" -> navController.navigateToStream()
                        "ministries" -> navController.navigateToMinistries()
                        "events" -> navController.navigateToEvents()
                    }
                },
            )

            // Floating transparent top bar over the hero
            HomeTopBar(
                onMenuClick = { navController.navigateToProfile() },
                onNotificationsClick = { /* TODO */ },
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

// ─────────────────────────── TOP BAR ───────────────────────────

@Composable
private fun HomeTopBar(
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HillsongWordmark(fontSize = 20, color = HillsongColors.White)
        Row {
            IconButton(onClick = onNotificationsClick) {
                // Bell icon (SVG-like using Material icon)
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = HillsongColors.White)
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = HillsongColors.White)
            }
        }
    }
}

// ─────────────────────────── BOTTOM NAV ───────────────────────────

@Composable
private fun HomeBottomNav(
    onHomeClick: () -> Unit,
    onEventsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0A).copy(alpha = 0.92f)),
    ) {
        // Subtle top border line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.06f))
                .align(Alignment.TopCenter),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavItem(
                label = "Home",
                icon = {
                    HomeNavIcon(color = it)
                },
                isActive = true,
                onClick = onHomeClick,
            )
            BottomNavItem(
                label = "Events",
                icon = {
                    EventsNavIcon(color = it)
                },
                isActive = false,
                onClick = onEventsClick,
            )
            BottomNavItem(
                label = "Profile",
                icon = {
                    ProfileNavIcon(color = it)
                },
                isActive = false,
                onClick = onProfileClick,
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: @Composable (color: Color) -> Unit,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val color = if (isActive) HillsongColors.Gold else HillsongColors.Gray500
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            icon(color)
            Text(
                text = label.uppercase(),
                style = TextStyle(
                    fontFamily = AppFonts.anta(),
                    fontSize = 10.sp,
                    color = color,
                    letterSpacing = 0.5.sp,
                ),
            )
            // Gold pill indicator under active tab
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(HillsongColors.Gold),
                )
            } else {
                Spacer(Modifier.height(3.dp))
            }
        }
    }
}

@Composable
private fun HomeNavIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        val path = Path().apply {
            moveTo(w * 0.125f, h * 0.417f)
            lineTo(w * 0.5f, h * 0.083f)
            lineTo(w * 0.875f, h * 0.417f)
            lineTo(w * 0.875f, h * 0.917f)
            cubicTo(w * 0.875f, h * 0.917f, w * 0.875f, h, w * 0.792f, h)
            lineTo(w * 0.625f, h)
            lineTo(w * 0.625f, h * 0.708f)
            lineTo(w * 0.375f, h * 0.708f)
            lineTo(w * 0.375f, h)
            lineTo(w * 0.208f, h)
            cubicTo(w * 0.125f, h, w * 0.125f, h * 0.917f, w * 0.125f, h * 0.917f)
            close()
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
private fun EventsNavIcon(color: Color) {
    Icon(
        imageVector = Icons.Default.DateRange,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(22.dp),
    )
}

@Composable
private fun ProfileNavIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        // Head circle
        drawCircle(color, radius = h * 0.167f, center = androidx.compose.ui.geometry.Offset(w / 2f, h * 0.333f), style = stroke)
        // Body arc
        val arcPath = Path().apply {
            moveTo(w * 0.167f, h * 0.875f)
            cubicTo(w * 0.167f, h * 0.583f, w * 0.317f, h * 0.583f, w * 0.5f, h * 0.583f)
            cubicTo(w * 0.683f, h * 0.583f, w * 0.833f, h * 0.583f, w * 0.833f, h * 0.875f)
        }
        drawPath(arcPath, color, style = stroke)
    }
}

// ─────────────────────────── CONTENT ───────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    upcomingEncounters: List<EncounterWithImageUrl> = emptyList(),
    isLoadingEncounters: Boolean = false,
    youtubeVideos: List<YouTubeVideo> = emptyList(),
    isLoadingVideos: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onVideoClick: (YouTubeVideo) -> Unit = {},
    onExploreTile: (String) -> Unit = {},
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize().padding(paddingValues),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            HeroPager(
                encounters = upcomingEncounters,
                isLoading = isLoadingEncounters,
            )

            Spacer(Modifier.height(4.dp))

            EditorialSectionHeader(
                title = "Watch",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )

            if (isLoadingVideos) {
                LoadingRow()
            } else if (youtubeVideos.isEmpty()) {
                EmptyRow(text = stringResource(Res.string.no_videos_available))
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(youtubeVideos) { video ->
                        WatchCard(video = video, onClick = { onVideoClick(video) })
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            EditorialSectionHeader(
                title = "Explore",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )

            ExploreGrid(onTile = onExploreTile)

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────── HERO ───────────────────────────

@Composable
private fun HeroPager(
    encounters: List<EncounterWithImageUrl>,
    isLoading: Boolean,
) {
    val bg = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp),
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
            )
        } else if (encounters.isEmpty()) {
            EmptyHero()
        } else {
            val pagerState = rememberPagerState(pageCount = { encounters.size })
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val e = encounters[page]
                HeroSlide(encounter = e.encounter, imageUrl = e.imageUrl, bg = bg)
            }
            // Pager dots — active: 20dp gold, inactive: 6dp white 35%
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(encounters.size) { i ->
                    val active = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (active) 20.dp else 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (active) HillsongColors.Gold else Color.White.copy(alpha = 0.35f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHero() {
    val bg = MaterialTheme.colorScheme.background
    Box(modifier = Modifier.fillMaxSize()) {
        rfm.hillsongptapp.util.media.AsyncImage(
            imageUrl = "https://images.unsplash.com/photo-1438232992991-995b7058bbb3?w=1920&q=100",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onFailure = {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
            },
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.35f),
                            0.3f to Color.Transparent,
                            0.6f to Color.Black.copy(alpha = 0.4f),
                            1.0f to bg,
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, end = 24.dp, bottom = 56.dp),
        ) {
            Text(
                text = "HILLSONG PT",
                style = TextStyle(
                    fontFamily = AppFonts.anta(),
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = HillsongColors.Gold,
                ),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Lisboa",
                style = TextStyle(
                    fontFamily = AppFonts.mogra(),
                    fontSize = 36.sp,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 40.sp,
                ),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "SUNDAYS · 11H & 17H",
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = Color.White.copy(alpha = 0.75f),
                ),
            )
        }
    }
}

@Composable
private fun HeroSlide(
    encounter: Encounter,
    imageUrl: String?,
    bg: Color,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        rfm.hillsongptapp.util.media.AsyncImage(
            imageUrl = imageUrl ?: "https://images.unsplash.com/photo-1438232992991-995b7058bbb3?w=1920&q=100",
            contentDescription = encounter.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onFailure = {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
            },
        )

        // 4-stop gradient fading to bg
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.40f),
                            0.3f to Color.Transparent,
                            0.6f to Color.Black.copy(alpha = 0.30f),
                            1.0f to bg,
                        ),
                    ),
                ),
        )

        // Hero content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, end = 24.dp, bottom = 56.dp),
        ) {
            // "NEXT ENCOUNTER" pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(HillsongColors.Error),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "NEXT ENCOUNTER",
                    style = TextStyle(
                        fontFamily = AppFonts.anta(),
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                        color = Color.White,
                    ),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = encounter.title,
                style = TextStyle(
                    fontFamily = AppFonts.mogra(),
                    fontSize = 36.sp,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 40.sp,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = buildHeroMeta(encounter),
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = Color.White.copy(alpha = 0.8f),
                ),
            )
        }
    }
}

private fun buildHeroMeta(e: Encounter): String {
    val parts = mutableListOf<String>()
    if (e.date.isNotBlank()) parts += e.date.uppercase()
    if (e.location.isNotBlank()) parts += e.location.uppercase()
    return parts.joinToString(" · ")
}

// ─────────────────────────── WATCH ───────────────────────────

@Composable
private fun WatchCard(video: YouTubeVideo, onClick: () -> Unit) {
    Column(modifier = Modifier.width(240.dp).clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            rfm.hillsongptapp.util.media.AsyncImage(
                imageUrl = video.thumbnailUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onFailure = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                },
            )
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.5f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.6f),
                            ),
                        ),
                    ),
            )
            // Play button
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Top) {
            // "H" avatar
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(HillsongColors.Gold),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "H",
                    style = TextStyle(
                        fontFamily = AppFonts.mogra(),
                        fontSize = 11.sp,
                        color = HillsongColors.Black,
                    ),
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                video.description?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = HillsongColors.Gray500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(180.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(2) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface),
            )
            Spacer(Modifier.width(12.dp))
        }
    }
}

@Composable
private fun EmptyRow(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────── EXPLORE GRID ───────────────────────────

private data class ExploreTile(val key: String, val label: String, val iconContent: @Composable (Color) -> Unit)

@Composable
private fun ExploreGrid(onTile: (String) -> Unit) {
    val gold = HillsongColors.Gold
    val tiles = listOf(
        ExploreTile("feed", "Feed") { c ->
            FeedTileIcon(c)
        },
        ExploreTile("calendar", "Calendar") { c ->
            CalendarTileIcon(c)
        },
        ExploreTile("kids", "Kids") { c ->
            KidsTileIcon(c)
        },
        ExploreTile("groups", "Groups") { c ->
            GroupsTileIcon(c)
        },
        ExploreTile("giving", "Giving") { c ->
            GivingTileIcon(c)
        },
        ExploreTile("stream", "Stream") { c ->
            StreamTileIcon(c)
        },
        ExploreTile("ministries", "Ministries") { c ->
            MinistriesTileIcon(c)
        },
        ExploreTile("events", "Events") { c ->
            EventsTileIcon(c)
        },
    )
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        tiles.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { tile ->
                    ExploreTileCell(
                        tile = tile,
                        modifier = Modifier.weight(1f),
                        onClick = { onTile(tile.key) },
                    )
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ExploreTileCell(
    tile: ExploreTile,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            tile.iconContent(HillsongColors.Gold)
            Spacer(Modifier.height(8.dp))
            Text(
                text = tile.label.uppercase(),
                style = TextStyle(
                    fontFamily = AppFonts.anta(),
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
        }
    }
}

// SVG-faithful tile icons

@Composable
private fun FeedTileIcon(color: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
        val stroke = Stroke(1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        val w = size.width; val h = size.height
        drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.167f, h * 0.25f), end = androidx.compose.ui.geometry.Offset(w * 0.833f, h * 0.25f), strokeWidth = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.167f, h * 0.5f), end = androidx.compose.ui.geometry.Offset(w * 0.833f, h * 0.5f), strokeWidth = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.167f, h * 0.75f), end = androidx.compose.ui.geometry.Offset(w * 0.583f, h * 0.75f), strokeWidth = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

@Composable
private fun CalendarTileIcon(color: Color) {
    Icon(imageVector = androidx.compose.material.icons.Icons.Default.DateRange, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
}

@Composable
private fun KidsTileIcon(color: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
        val stroke = Stroke(1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        val w = size.width; val h = size.height
        drawCircle(color, radius = h * 0.22f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.33f), style = stroke)
        val path = Path()
        path.moveTo(w * 0.1f, h * 0.92f)
        path.cubicTo(w * 0.1f, h * 0.67f, w * 0.9f, h * 0.67f, w * 0.9f, h * 0.92f)
        drawPath(path, color, style = stroke)
    }
}

@Composable
private fun GroupsTileIcon(color: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
        val stroke = Stroke(1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        val w = size.width; val h = size.height
        drawCircle(color, radius = h * 0.167f, center = androidx.compose.ui.geometry.Offset(w * 0.375f, h * 0.417f), style = stroke)
        drawCircle(color, radius = h * 0.139f, center = androidx.compose.ui.geometry.Offset(w * 0.708f, h * 0.5f), style = stroke)
        val bodyPath = Path().apply {
            moveTo(w * 0.125f, h * 0.833f)
            cubicTo(w * 0.125f, h * 0.583f, w * 0.242f, h * 0.583f, w * 0.375f, h * 0.583f)
            cubicTo(w * 0.508f, h * 0.583f, w * 0.625f, h * 0.583f, w * 0.625f, h * 0.833f)
        }
        drawPath(bodyPath, color, style = stroke)
    }
}

@Composable
private fun GivingTileIcon(color: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
        val stroke = Stroke(1.8.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round)
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.875f)
            cubicTo(w * 0.5f, h * 0.875f, w * 0.167f, h * 0.667f, w * 0.167f, h * 0.375f)
            cubicTo(w * 0.167f, h * 0.167f, w * 0.5f, h * 0.042f, w * 0.5f, h * 0.25f)
            cubicTo(w * 0.5f, h * 0.042f, w * 0.833f, h * 0.167f, w * 0.833f, h * 0.375f)
            cubicTo(w * 0.833f, h * 0.667f, w * 0.5f, h * 0.875f, w * 0.5f, h * 0.875f)
            close()
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
private fun StreamTileIcon(color: Color) {
    Icon(imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
}

@Composable
private fun MinistriesTileIcon(color: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
        val stroke = Stroke(1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        val w = size.width; val h = size.height
        drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.5f, 0f), end = androidx.compose.ui.geometry.Offset(w * 0.5f, h), strokeWidth = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, start = androidx.compose.ui.geometry.Offset(w * 0.208f, h * 0.375f), end = androidx.compose.ui.geometry.Offset(w * 0.792f, h * 0.375f), strokeWidth = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

@Composable
private fun EventsTileIcon(color: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
        val stroke = Stroke(1.8.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round)
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.083f)
            lineTo(w * 0.625f, h * 0.333f)
            lineTo(w * 0.875f, h * 0.375f)
            lineTo(w * 0.688f, h * 0.563f)
            lineTo(w * 0.75f, h * 0.833f)
            lineTo(w * 0.5f, h * 0.708f)
            lineTo(w * 0.25f, h * 0.833f)
            lineTo(w * 0.313f, h * 0.563f)
            lineTo(w * 0.125f, h * 0.375f)
            lineTo(w * 0.375f, h * 0.333f)
            close()
        }
        drawPath(path, color, style = stroke)
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeContent(
            paddingValues = PaddingValues(0.dp),
            upcomingEncounters = emptyList(),
            isLoadingEncounters = false,
            youtubeVideos = emptyList(),
            isLoadingVideos = false,
            isRefreshing = false,
        )
    }
}
