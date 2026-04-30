package rfm.hillsongptapp.feature.events

import AppFonts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.koin.compose.koinInject
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.core.network.api.ProfileApiService
import rfm.hillsongptapp.core.network.api.UserProfile
import rfm.hillsongptapp.core.network.result.NetworkResult

@Composable
fun MyQrScreen(
    navController: NavHostController,
    profileApiService: ProfileApiService = koinInject(),
) {
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        when (val result = profileApiService.getProfile()) {
            is NetworkResult.Success -> {
                profile = result.data
                isLoading = false
            }
            is NetworkResult.Error -> {
                error = result.exception.message ?: "Failed to load profile"
                isLoading = false
            }
            is NetworkResult.Loading -> Unit
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top bar
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "O MEU QR",
                    style = TextStyle(
                        fontFamily = AppFonts.anta(),
                        fontSize = 14.sp,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Spacer(Modifier.height(32.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HillsongColors.Gold)
                    }
                }
                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = error ?: "Error",
                            style = TextStyle(fontFamily = AppFonts.andika(), fontSize = 13.sp, color = HillsongColors.Error),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                }
                profile != null -> {
                    val qrToken = profile!!.qrToken

                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Profile info
                        Text(
                            text = profile!!.fullName.ifBlank { profile!!.email },
                            style = TextStyle(
                                fontFamily = AppFonts.andika(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text = "Mostra este QR a um responsável para registo de presença",
                            style = TextStyle(
                                fontFamily = AppFonts.andika(),
                                fontSize = 13.sp,
                                color = HillsongColors.Gray500,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                            ),
                        )

                        if (qrToken != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    val painter = rememberQrCodePainter(qrToken) {
                                        shapes {
                                            ball = QrBallShape.circle()
                                            darkPixel = QrPixelShape.roundCorners()
                                            frame = QrFrameShape.roundCorners(.25f)
                                        }
                                        colors {
                                            dark = QrBrush.solid(Color.Black)
                                            frame = QrBrush.solid(Color.Black)
                                        }
                                    }
                                    Icon(
                                        painter = painter,
                                        contentDescription = "QR de check-in",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = Color.Unspecified,
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(200.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "QR não disponível.\nTente de novo mais tarde.",
                                    style = TextStyle(
                                        fontFamily = AppFonts.andika(),
                                        fontSize = 13.sp,
                                        color = HillsongColors.Gray500,
                                        textAlign = TextAlign.Center,
                                    ),
                                )
                            }
                        }

                        Text(
                            text = profile!!.email,
                            style = TextStyle(
                                fontFamily = AppFonts.andika(),
                                fontSize = 12.sp,
                                color = HillsongColors.Gray500,
                            ),
                        )
                    }
                }
            }
        }
    }
}
