package rfm.hillsongptapp.feature.videoplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import rfm.hillsongptapp.logging.LoggerHelper

@Composable
actual fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier
) {
    val videoId = remember(videoUrl) {
        val extractedId = extractYouTubeVideoId(videoUrl).trim()
        LoggerHelper.logDebug("Video URL: $videoUrl", "VideoPlayer")
        LoggerHelper.logDebug("Extracted Video ID: '$extractedId'", "VideoPlayer")
        extractedId
    }

    // Use WebView-based player with proper origin parameter support
    WebViewYouTubePlayer(
        videoId = videoId,
        modifier = modifier,
        autoplay = false
    )
}

@Composable
actual fun YouTubePlayerScreen(
    videoId: String,
    onBack: () -> Unit
) {
    LoggerHelper.logDebug("YouTubePlayerScreen composing with videoId=$videoId", "YouTubePlayerScreen")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Use WebView-based player with proper origin parameter support
        WebViewYouTubePlayer(
            videoId = videoId,
            modifier = Modifier.fillMaxSize(),
            autoplay = false
        )

        // Close button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .background(Color.White.copy(alpha = 0.1f), shape = CircleShape)
                .clickable { onBack() }
                .size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
