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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier
) {
    // TODO: Implement YouTube player for iOS
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "YouTube player not yet implemented for iOS\nVideo: $videoUrl",
            color = Color.White
        )
    }
}

@Composable
actual fun YouTubePlayerScreen(
    videoId: String,
    onBack: () -> Unit
) {
    // TODO: Implement YouTube player screen for iOS
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Text(
            text = "YouTube player not yet implemented for iOS\nVideo ID: $videoId",
            color = Color.White
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
