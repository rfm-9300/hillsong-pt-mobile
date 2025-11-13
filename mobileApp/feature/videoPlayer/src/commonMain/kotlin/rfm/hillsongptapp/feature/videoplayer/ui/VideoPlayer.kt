package rfm.hillsongptapp.feature.videoplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Extract YouTube video ID from various URL formats
 * Supports: youtube.com/watch?v=ID, youtu.be/ID, youtube.com/embed/ID, youtube.com/shorts/ID
 */
fun extractYouTubeVideoId(url: String): String {
    val trimmed = url.trim()

    // If it's already just an ID (11 chars typical), return it
    if (!trimmed.contains("://") && trimmed.length >= 10) {
        return trimmed
    }

    // Extract 'v=' parameter
    val vParam = Regex("[?&]v=([A-Za-z0-9_-]+)").find(trimmed)
    if (vParam != null) {
        return vParam.groupValues[1]
    }

    // Extract from youtu.be short links
    val youtuBe = Regex("youtu\\.be/([A-Za-z0-9_-]+)").find(trimmed)
    if (youtuBe != null) {
        return youtuBe.groupValues[1]
    }

    // Extract from /embed/ or /shorts/ paths
    val embedOrShorts = Regex("/(embed|shorts)/([A-Za-z0-9_-]+)").find(trimmed)
    if (embedOrShorts != null) {
        return embedOrShorts.groupValues[2]
    }

    // Fallback: return the original URL
    return trimmed
}

/**
 * Platform-specific video player composable
 * Expects a YouTube video URL and displays it using the platform's native player
 */
@Composable
expect fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
)

@Composable
expect fun YouTubePlayerScreen(
    videoId: String,
    onBack: () -> Unit = {}
)
