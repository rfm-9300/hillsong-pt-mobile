package rfm.hillsongptapp.feature.home.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import hillsongptapp.feature.home.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.designsystem.BottomBarDestination
import rfm.hillsongptapp.core.designsystem.HillsongBottomAppBar
import rfm.hillsongptapp.core.designsystem.HillsongTopAppBar
import rfm.hillsongptapp.core.designsystem.theme.AppTheme
import rfm.hillsongptapp.core.navigation.navigateToSettings
import rfm.hillsongptapp.core.navigation.navigateToStream
import rfm.hillsongptapp.core.navigation.navigateToProfile
import rfm.hillsongptapp.core.navigation.navigateToMinistries
import rfm.hillsongptapp.core.navigation.navigateToKids
import rfm.hillsongptapp.core.navigation.navigateToGroups
import rfm.hillsongptapp.core.navigation.navigateToGiving
import rfm.hillsongptapp.core.navigation.navigateToFeed
import rfm.hillsongptapp.core.navigation.navigateToEvents
import rfm.hillsongptapp.core.navigation.navigateToYouTubeVideo
import rfm.hillsongptapp.core.network.api.Encounter

@Composable
fun homeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentRoute by remember { mutableStateOf(BottomBarDestination.Home.route) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HillsongTopAppBar(
                title = stringResource(Res.string.app_name),
                onMenuClick = { showMenu = true }
            )
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.menu_settings)) },
                    onClick = {
                        showMenu = false
                        navController.navigateToSettings()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.menu_about_us)) },
                    onClick = { 
                        showMenu = false
                        // Handle about us click
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.menu_contact)) },
                    onClick = { 
                        showMenu = false
                        // Handle contact click
                    }
                )
            }
        },
        bottomBar = {
            HillsongBottomAppBar(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    currentRoute = route
                }
            )
        }
    ) { paddingValues ->
        HomeContent(
            paddingValues = paddingValues,
            upcomingEncounters = uiState.upcomingEncounters,
            isLoadingEncounters = uiState.isLoadingEncounters,
            youtubeVideos = uiState.youtubeVideos,
            isLoadingVideos = uiState.isLoadingVideos,
            onVideoClick = { video ->
                navController.navigateToYouTubeVideo(video.id, video.videoUrl)
            },
            onStream = { navController.navigateToStream() },
            onSettings = { navController.navigateToSettings() },
            onProfile = { navController.navigateToProfile() },
            onMinistries = { navController.navigateToMinistries() },
            onKids = { navController.navigateToKids() },
            onGroups = { navController.navigateToGroups() },
            onGiving = { navController.navigateToGiving() },
            onFeed = { navController.navigateToFeed() },
            onEvents = { navController.navigateToEvents() }
        )
    }
}

@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    upcomingEncounters: List<EncounterWithImageUrl> = emptyList(),
    isLoadingEncounters: Boolean = false,
    youtubeVideos: List<rfm.hillsongptapp.core.network.api.YouTubeVideo> = emptyList(),
    isLoadingVideos: Boolean = false,
    onVideoClick: (rfm.hillsongptapp.core.network.api.YouTubeVideo) -> Unit = {},
    onStream: () -> Unit = {},
    onSettings: () -> Unit = {},
    onProfile: () -> Unit = {},
    onMinistries: () -> Unit = {},
    onKids: () -> Unit = {},
    onGroups: () -> Unit = {},
    onGiving: () -> Unit = {},
    onFeed: () -> Unit = {},
    onEvents: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WelcomeSection()
        UpcomingServiceCard()
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.watch_our_videos),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        YouTubeVideosCarousel(
            videos = youtubeVideos,
            isLoading = isLoadingVideos,
            onVideoClick = onVideoClick
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.upcoming_encounters),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        UpcomingEncountersCarousel(
            encounters = upcomingEncounters,
            isLoading = isLoadingEncounters,
            onEncounterClick = { onEvents() }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.explore_modules),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        ModuleActionCards(
            onStream = onStream,
            onSettings = onSettings,
            onProfile = onProfile,
            onMinistries = onMinistries,
            onKids = onKids,
            onGroups = onGroups,
            onGiving = onGiving,
            onFeed = onFeed,
            onEvents = onEvents
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.daily_scripture),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        ScriptureCard()
    }
}

@Composable
fun WelcomeSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.welcome_to),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.welcome_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UpcomingServiceCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.height(180.dp)) {
            // This would typically be an actual image loaded from a resource or URL
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                // If you have an actual image resource:
                // Image(
                //     painter = painterResource("drawable/service_image.png"),
                //     contentDescription = "Sunday Service",
                //     contentScale = ContentScale.Crop,
                //     modifier = Modifier.fillMaxSize()
                // )
            }
            
            // Gradient overlay to ensure text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stringResource(Res.string.next_sunday_service),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = stringResource(Res.string.join_us_sunday),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = stringResource(Res.string.service_time_location),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ModuleActionCards(
    onStream: () -> Unit = {},
    onSettings: () -> Unit = {},
    onProfile: () -> Unit = {},
    onMinistries: () -> Unit = {},
    onKids: () -> Unit = {},
    onGroups: () -> Unit = {},
    onGiving: () -> Unit = {},
    onFeed: () -> Unit = {},
    onEvents: () -> Unit = {}
) {
    val modules = listOf(
        Triple(stringResource(Res.string.module_stream), Icons.Default.Settings, onStream),
        Triple(stringResource(Res.string.module_settings), Icons.Default.Settings, onSettings),
        Triple(stringResource(Res.string.module_profile), Icons.Default.AccountCircle, onProfile),
        Triple(stringResource(Res.string.module_ministries), Icons.Default.AccountCircle, onMinistries),
        Triple(stringResource(Res.string.module_kids), Icons.Default.Person, onKids),
        Triple(stringResource(Res.string.module_groups), Icons.Default.Person, onGroups),
        Triple(stringResource(Res.string.module_giving), Icons.Default.Clear, onGiving),
        Triple(stringResource(Res.string.module_feed), Icons.Default.Create, onFeed),
        Triple(stringResource(Res.string.module_events), Icons.Default.ShoppingCart, onEvents),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in modules.chunked(3)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for ((title, icon, onClick) in row) {
                    ActionCard(
                        icon = icon,
                        title = title,
                        modifier = Modifier.weight(1f),
                        onClick = onClick
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun UpcomingEncountersCarousel(
    encounters: List<EncounterWithImageUrl>,
    isLoading: Boolean,
    onEncounterClick: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (encounters.isEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.no_upcoming_encounters),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(encounters) { encounterWithUrl ->
                EncounterCard(
                    encounter = encounterWithUrl.encounter,
                    imageUrl = encounterWithUrl.imageUrl,
                    onClick = onEncounterClick
                )
            }
        }
    }
}

@Composable
fun EncounterCard(
    encounter: Encounter,
    imageUrl: String?,
    onClick: () -> Unit
) {
    // Log for debugging
    rfm.hillsongptapp.logging.LoggerHelper.logDebug(
        "EncounterCard - Title: ${encounter.title}, ImageURL: $imageUrl",
        "EncounterCard"
    )
    
    Surface(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Display image if available, otherwise show gradient background
            if (imageUrl != null) {
                rfm.hillsongptapp.logging.LoggerHelper.logDebug(
                    "Loading image from URL: $imageUrl",
                    "EncounterCard"
                )
                rfm.hillsongptapp.util.media.AsyncImage(
                    imageUrl = imageUrl,
                    contentDescription = encounter.title,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onFailure = {
                        rfm.hillsongptapp.logging.LoggerHelper.logDebug(
                            "Image failed to load: $imageUrl",
                            "EncounterCard"
                        )
                        // Fallback to gradient background if image fails to load
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                )
            } else {
                rfm.hillsongptapp.logging.LoggerHelper.logDebug(
                    "No image URL provided, showing gradient",
                    "EncounterCard"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = encounter.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = encounter.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(Res.string.organized_by, encounter.organizerName),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun YouTubeVideosCarousel(
    videos: List<rfm.hillsongptapp.core.network.api.YouTubeVideo>,
    isLoading: Boolean,
    onVideoClick: (rfm.hillsongptapp.core.network.api.YouTubeVideo) -> Unit = {}
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (videos.isEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.no_videos_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(videos) { video ->
                YouTubeVideoCard(
                    video = video,
                    onClick = { onVideoClick(video) }
                )
            }
        }
    }
}

@Composable
fun YouTubeVideoCard(
    video: rfm.hillsongptapp.core.network.api.YouTubeVideo,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                rfm.hillsongptapp.util.media.AsyncImage(
                    imageUrl = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onFailure = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                )
            }
            
            // Video info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                video.description?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ScriptureCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(Res.string.scripture_verse),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.scripture_reference),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    AppTheme {
        Surface {
            HomeContent(
                paddingValues = PaddingValues(16.dp),
                upcomingEncounters = emptyList(),
                isLoadingEncounters = false,
                youtubeVideos = emptyList(),
                isLoadingVideos = false,
                onVideoClick = {},
                onStream = {},
                onSettings = {},
                onProfile = {},
                onMinistries = {},
                onKids = {},
                onGroups = {},
                onGiving = {},
                onFeed = {},
                onEvents = {}
            )
        }
    }
}