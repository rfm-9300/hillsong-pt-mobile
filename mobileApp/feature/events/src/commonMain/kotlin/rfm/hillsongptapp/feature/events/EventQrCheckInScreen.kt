package rfm.hillsongptapp.feature.events

import AppFonts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.feature.qrcode.QrCodeValidator
import rfm.hillsongptapp.feature.qrcode.QrScannerScreen
import rfm.hillsongptapp.feature.qrcode.QrValidationResult

@Composable
fun EventQrCheckInScreen(
    eventId: String,
    navController: NavHostController,
    viewModel: EventQrCheckInViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var scanSession by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        key(scanSession) {
            QrScannerScreen(
                title = "Check-in do evento",
                instructions = "Lê o QR do participante para confirmar a presença.",
                onNavigateBack = { navController.popBackStack() },
                onQrScanned = { token -> viewModel.checkIn(eventId, token) },
                validator = QrCodeValidator { value ->
                    if (value.isBlank()) {
                        QrValidationResult.Invalid("QR inválido.")
                    } else {
                        QrValidationResult.Valid
                    }
                }
            )
        }

        if (uiState.isCheckingIn) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = HillsongColors.Gold)
            }
        }

        val attendance = uiState.checkedInAttendance
        val error = uiState.error
        if (attendance != null || error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (attendance != null) "Check-in confirmado" else "Check-in falhou",
                            style = TextStyle(
                                fontFamily = AppFonts.andika(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (attendance != null) HillsongColors.Gold else HillsongColors.Error,
                                textAlign = TextAlign.Center
                            ),
                        )
                        Text(
                            text = attendance?.user?.fullName ?: error.orEmpty(),
                            style = TextStyle(
                                fontFamily = AppFonts.andika(),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            ),
                        )
                        Button(
                            onClick = {
                                viewModel.scanAgain()
                                scanSession += 1
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HillsongColors.Gold),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                text = "Ler outro QR",
                                style = TextStyle(fontFamily = AppFonts.andika(), fontWeight = FontWeight.Bold)
                            )
                        }
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                text = "Voltar ao evento",
                                style = TextStyle(fontFamily = AppFonts.andika())
                            )
                        }
                    }
                }
            }
        }
    }
}
