package rfm.hillsongptapp.feature.groups

import AppFonts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import hillsongptapp.feature.groups.generated.resources.Res
import hillsongptapp.feature.groups.generated.resources.groups_contact_whatsapp
import hillsongptapp.feature.groups.generated.resources.groups_error
import hillsongptapp.feature.groups.generated.resources.groups_leader
import hillsongptapp.feature.groups.generated.resources.groups_members
import hillsongptapp.feature.groups.generated.resources.groups_open_maps
import hillsongptapp.feature.groups.generated.resources.groups_schedule
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.core.network.api.Group
import rfm.hillsongptapp.core.network.api.GroupsApiService
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.util.media.AsyncImage
import rfm.hillsongptapp.util.platform.UrlOpener

@Composable
fun GroupDetailScreen(
    groupId: String,
    navController: NavHostController,
    groupsApiService: GroupsApiService = koinInject(),
    baseUrl: String = koinInject(named("baseUrl")),
) {
    var state by remember { mutableStateOf<NetworkResult<Group>>(NetworkResult.Loading) }

    LaunchedEffect(groupId) {
        state = groupsApiService.getGroup(groupId)
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            DetailTopBar(onBackClick = { navController.popBackStack() })

            when (val current = state) {
                is NetworkResult.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HillsongColors.Gold)
                    }
                }
                is NetworkResult.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(Res.string.groups_error),
                            style = TextStyle(
                                fontFamily = AppFonts.andika(),
                                fontSize = 14.sp,
                                color = HillsongColors.Gray500,
                            ),
                        )
                    }
                }
                is NetworkResult.Success -> {
                    val group = current.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        item {
                            if (group.imagePath != null) {
                                AsyncImage(
                                    imageUrl = "$baseUrl/api/files/${group.imagePath}",
                                    contentDescription = group.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp),
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .background(HillsongColors.Gold.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = labelForMinistry(group.ministry),
                                        style = TextStyle(
                                            fontFamily = AppFonts.anta(),
                                            fontSize = 16.sp,
                                            letterSpacing = 2.sp,
                                            color = HillsongColors.Gold,
                                        ),
                                    )
                                }
                            }
                        }

                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = labelForMinistry(group.ministry),
                                    style = TextStyle(
                                        fontFamily = AppFonts.anta(),
                                        fontSize = 11.sp,
                                        letterSpacing = 2.sp,
                                        color = HillsongColors.Gold,
                                    ),
                                )
                                Text(
                                    text = group.name,
                                    style = TextStyle(
                                        fontFamily = AppFonts.andika(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        lineHeight = 32.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    ),
                                )
                                Text(
                                    text = group.description,
                                    style = TextStyle(
                                        fontFamily = AppFonts.andika(),
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        color = HillsongColors.Gray500,
                                    ),
                                )
                            }
                        }

                        item {
                            InfoSection(
                                title = stringResource(Res.string.groups_schedule),
                                value = "${group.meetingDay.name.lowercase().replaceFirstChar { it.titlecase() }} • ${group.meetingTime} • ${frequencyLabel(group.frequency)}",
                            )
                        }

                        item {
                            InfoSection(
                                title = stringResource(Res.string.groups_leader),
                                value = "${group.leaderName} • ${group.leaderContact}",
                            )
                        }

                        item {
                            InfoSection(
                                title = stringResource(Res.string.groups_members),
                                value = if (group.maxMembers != null) {
                                    "${group.currentMembers}/${group.maxMembers}"
                                } else {
                                    "${group.currentMembers}"
                                },
                            )
                        }

                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = group.location.addressLine,
                                    style = TextStyle(
                                        fontFamily = AppFonts.andika(),
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    ),
                                )
                                Text(
                                    text = listOfNotNull(
                                        group.location.city,
                                        group.location.region,
                                        group.location.postalCode
                                    ).joinToString(" • "),
                                    style = TextStyle(
                                        fontFamily = AppFonts.andika(),
                                        fontSize = 13.sp,
                                        color = HillsongColors.Gray500,
                                    ),
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Button(
                                    onClick = {
                                        UrlOpener.openUrl("https://wa.me/${sanitizePhone(group.leaderContact)}")
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = group.isJoinable,
                                ) {
                                    Text(stringResource(Res.string.groups_contact_whatsapp))
                                }
                                Button(
                                    onClick = {
                                        UrlOpener.openUrl(
                                            "https://www.google.com/maps/search/?api=1&query=${group.location.latitude},${group.location.longitude}"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(stringResource(Res.string.groups_open_maps))
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
private fun DetailTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun InfoSection(title: String, value: String) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = AppFonts.anta(),
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = HillsongColors.Gray500,
            ),
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = AppFonts.andika(),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
    }
}

private fun sanitizePhone(raw: String): String = raw.filter { it.isDigit() }
