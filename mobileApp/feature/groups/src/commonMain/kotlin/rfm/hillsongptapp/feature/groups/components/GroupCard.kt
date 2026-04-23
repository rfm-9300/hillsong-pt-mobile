package rfm.hillsongptapp.feature.groups.components

import AppFonts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.core.network.api.GroupSummary
import rfm.hillsongptapp.util.media.AsyncImage

@Composable
fun GroupCard(
    group: GroupSummary,
    imageUrl: String?,
    ministryLabel: String,
    dayLabel: String,
    frequencyLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        if (imageUrl != null) {
            AsyncImage(
                imageUrl = imageUrl,
                contentDescription = group.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(HillsongColors.Gold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = ministryLabel,
                    style = TextStyle(
                        fontFamily = AppFonts.anta(),
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        color = HillsongColors.Gold,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(HillsongColors.Gold.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = ministryLabel,
                    style = TextStyle(
                        fontFamily = AppFonts.anta(),
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = HillsongColors.Gold,
                    ),
                )
            }

            Text(
                text = group.name,
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 22.sp,
                ),
            )

            Text(
                text = group.description,
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontSize = 13.sp,
                    color = HillsongColors.Gray500,
                    lineHeight = 18.sp,
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetaBlock(label = dayLabel, value = group.meetingTime)
                MetaBlock(label = group.city, value = frequencyLabel)
                MetaBlock(
                    label = if (group.isJoinable) "OPEN" else "PAUSED",
                    value = if (group.maxMembers != null) "${group.currentMembers}/${group.maxMembers}" else "${group.currentMembers}",
                )
            }
        }
    }
}

@Composable
private fun MetaBlock(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = AppFonts.anta(),
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                color = HillsongColors.Gray500,
            ),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.size(0.dp))
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
        }
    }
}
