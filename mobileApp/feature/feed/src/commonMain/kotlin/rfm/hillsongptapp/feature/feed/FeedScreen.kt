package rfm.hillsongptapp.feature.feed

import AppFonts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.koin.compose.koinInject
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.util.media.AsyncImage

@Composable
fun FeedScreen(
    navController: NavHostController,
    viewModel: FeedViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    FeedScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = { navController.popBackStack() },
    )
}

@Composable
private fun FeedScreenContent(
    uiState: FeedUiState,
    onEvent: (FeedEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    val listState = rememberLazyListState()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Devotional", "Community", "Music", "Teaching")

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                if (lastVisible != null &&
                    lastVisible >= uiState.feedItems.size - 3 &&
                    uiState.hasMorePages &&
                    !uiState.isLoading
                ) {
                    onEvent(FeedEvent.LoadMore)
                }
            }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading && uiState.feedItems.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HillsongColors.Gold)
                    }
                }
                uiState.feedItems.isEmpty() && !uiState.isLoading -> {
                    FeedEmptyState(
                        isSearching = uiState.searchQuery.isNotBlank(),
                        onRetry = { onEvent(FeedEvent.RetryLoad) },
                        onBackClick = onBackClick,
                    )
                }
                else -> {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        item {
                            FeedHeader(
                                onBackClick = onBackClick,
                                onSearchClick = { onEvent(FeedEvent.Refresh) },
                            )
                        }
                        item {
                            FilterChips(
                                filters = filters,
                                selected = selectedFilter,
                                onSelect = { selectedFilter = it },
                            )
                        }
                        items(uiState.feedItems, key = { it.id }) { item ->
                            FeedItemCard(
                                item = item,
                                onLikeClick = { onEvent(FeedEvent.LikePost(item.id)) },
                                modifier = Modifier.animateItem(),
                            )
                            Spacer(Modifier.height(24.dp))
                        }
                        if (uiState.hasMorePages) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(2.dp)
                                            .background(HillsongColors.Gold.copy(alpha = 0.7f)),
                                    )
                                }
                            }
                        }
                        item { Spacer(Modifier.height(60.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedHeader(onBackClick: () -> Unit, onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = "Feed",
            style = TextStyle(
                fontFamily = AppFonts.mogra(),
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp,
            ),
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onSearchClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun FilterChips(filters: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { filter ->
            val isActive = filter == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isActive) HillsongColors.Gold else MaterialTheme.colorScheme.surface)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    text = filter,
                    style = TextStyle(
                        fontFamily = AppFonts.andika(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isActive) HillsongColors.Black else MaterialTheme.colorScheme.onBackground,
                    ),
                )
            }
        }
    }
}

@Composable
private fun FeedItemCard(item: FeedItem, onLikeClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f),
        ) {
            if (item.imageUrl != null) {
                AsyncImage(
                    imageUrl = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onFailure = {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
                    },
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
            }

            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.4f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.85f),
                        ),
                    ),
                ),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "POST",
                    style = TextStyle(
                        fontFamily = AppFonts.anta(),
                        fontSize = 10.sp,
                        color = HillsongColors.Gold,
                        letterSpacing = 1.5.sp,
                    ),
                )
            }

            Text(
                text = item.title,
                style = TextStyle(
                    fontFamily = AppFonts.mogra(),
                    fontSize = 22.sp,
                    color = Color.White,
                    lineHeight = 26.sp,
                    letterSpacing = (-0.3).sp,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)) {
            Text(
                text = item.content,
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontSize = 14.sp,
                    color = HillsongColors.Gray500,
                    lineHeight = 21.sp,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${item.timeAgo.uppercase()} · 3 MIN READ",
                    style = TextStyle(
                        fontFamily = AppFonts.andika(),
                        fontSize = 11.sp,
                        color = HillsongColors.Gray500,
                        letterSpacing = 0.5.sp,
                    ),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(onClick = onLikeClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            if (item.isLikedByCurrentUser) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = HillsongColors.Gold,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        text = item.likeCount.toString(),
                        style = TextStyle(
                            fontFamily = AppFonts.andika(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedEmptyState(isSearching: Boolean, onRetry: () -> Unit, onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                "Feed",
                style = TextStyle(fontFamily = AppFonts.mogra(), fontSize = 28.sp, color = MaterialTheme.colorScheme.onBackground, letterSpacing = (-0.5).sp),
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 48.dp),
            ) {
                Text(
                    text = if (isSearching) "No posts found" else "No posts yet",
                    style = TextStyle(fontFamily = AppFonts.mogra(), fontSize = 22.sp, color = MaterialTheme.colorScheme.onBackground),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (isSearching) "Try adjusting your search" else "Check back soon for new content.",
                    style = TextStyle(fontFamily = AppFonts.andika(), fontSize = 13.sp, color = HillsongColors.Gray500),
                    textAlign = TextAlign.Center,
                )
                if (!isSearching) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(28.dp))
                            .background(HillsongColors.Gold)
                            .clickable(onClick = onRetry)
                            .padding(horizontal = 32.dp, vertical = 14.dp),
                    ) {
                        Text(
                            "RETRY",
                            style = TextStyle(fontFamily = AppFonts.andika(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HillsongColors.Black),
                        )
                    }
                }
            }
        }
    }
}
