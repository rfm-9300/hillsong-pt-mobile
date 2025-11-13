package rfm.hillsongptapp.feature.videoplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.ilyapavlovskii.multiplatform.youtubeplayer.YouTubePlayer
import io.github.ilyapavlovskii.multiplatform.youtubeplayer.YouTubePlayerHostState
import io.github.ilyapavlovskii.multiplatform.youtubeplayer.YouTubePlayerState
import io.github.ilyapavlovskii.multiplatform.youtubeplayer.SimpleYouTubePlayerOptionsBuilder
import io.github.ilyapavlovskii.multiplatform.youtubeplayer.model.YouTubeExecCommand
import io.github.ilyapavlovskii.multiplatform.youtubeplayer.YouTubeVideoId
import rfm.hillsongptapp.logging.LoggerHelper

@Composable
actual fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier
) {
    val videoId = remember(videoUrl) {
        val extractedId = extractYouTubeVideoId(videoUrl)
        LoggerHelper.logDebug("Video URL: $videoUrl", "VideoPlayer")
        LoggerHelper.logDebug("Extracted Video ID: $extractedId", "VideoPlayer")
        extractedId
    }

    val hostState = remember { YouTubePlayerHostState() }

    LaunchedEffect(hostState.currentState) {
        when (val state = hostState.currentState) {
            is YouTubePlayerState.Ready -> {
                LoggerHelper.logDebug("YouTubePlayer Ready - Loading videoId=$videoId", "VideoPlayer")
                hostState.executeCommand(YouTubeExecCommand.LoadVideo(YouTubeVideoId(videoId)))
            }
            is YouTubePlayerState.Error -> {
                LoggerHelper.logDebug("YouTubePlayer Error: ${state.message}", "VideoPlayer")
            }
            else -> {
                LoggerHelper.logDebug("YouTubePlayer State: $state", "VideoPlayer")
            }
        }
    }

    YouTubePlayer(
        modifier = modifier,
        hostState = hostState,
        options = SimpleYouTubePlayerOptionsBuilder.builder {
            autoplay(false)
            controls(true)
            rel(false)
            fullscreen = true
        }
    )
}

@Composable
actual fun YouTubePlayerScreen(
    videoId: String,
    onBack: () -> Unit
) {
    LoggerHelper.logDebug("YouTubePlayerScreen composing with videoId=$videoId", "YouTubePlayerScreen")

    val hostState = remember { YouTubePlayerHostState() }

    LaunchedEffect(hostState.currentState) {
        when (val state = hostState.currentState) {
            is YouTubePlayerState.Ready -> {
                LoggerHelper.logDebug("YouTubePlayer Ready - Loading videoId=$videoId", "YouTubePlayerScreen")
                hostState.executeCommand(YouTubeExecCommand.LoadVideo(YouTubeVideoId(videoId)))
            }
            is YouTubePlayerState.Error -> {
                LoggerHelper.logDebug("YouTubePlayer Error: ${state.message}", "YouTubePlayerScreen")
            }
            else -> {
                LoggerHelper.logDebug("YouTubePlayer State: $state", "YouTubePlayerScreen")
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        YouTubePlayer(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(16f / 9f)
                .align(Alignment.Center),
            hostState = hostState,
            options = SimpleYouTubePlayerOptionsBuilder.builder {
                autoplay(true)
                controls(true)
                rel(false)
                fullscreen = true
            }
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
