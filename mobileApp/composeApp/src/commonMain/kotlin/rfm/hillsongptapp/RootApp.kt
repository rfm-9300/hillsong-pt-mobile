package rfm.hillsongptapp


import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import rfm.hillsongptapp.core.navigation.LoginGraph
import rfm.hillsongptapp.core.navigation.homeGraph
import rfm.hillsongptapp.core.navigation.kidsGraph
import rfm.hillsongptapp.core.navigation.loginGraph
import rfm.hillsongptapp.feature.auth.LoginScreen
import rfm.hillsongptapp.feature.stream.StreamScreen
import rfm.hillsongptapp.feature.settings.SettingsScreen
import rfm.hillsongptapp.feature.profile.ProfileScreen
import rfm.hillsongptapp.feature.ministries.MinistriesScreen
import rfm.hillsongptapp.feature.kids.KidsScreen
import rfm.hillsongptapp.feature.kids.ui.KidsManagementScreen
import rfm.hillsongptapp.feature.kids.ui.registration.ChildRegistrationScreen
import rfm.hillsongptapp.feature.kids.ui.checkin.QRCodeDisplayWrapper
import rfm.hillsongptapp.feature.kids.ui.checkout.CheckOutScreen
import rfm.hillsongptapp.feature.kids.ui.edit.ChildEditScreen
import rfm.hillsongptapp.feature.kids.ui.reports.ReportsScreen
import rfm.hillsongptapp.feature.kids.ui.staff.StaffDashboardScreen
import rfm.hillsongptapp.feature.kids.ui.staff.QRCodeScannerScreen
import rfm.hillsongptapp.feature.kids.ui.staff.CheckInVerificationScreen
import rfm.hillsongptapp.feature.groups.GroupsScreen
import rfm.hillsongptapp.feature.giving.GivingScreen
import rfm.hillsongptapp.feature.feed.FeedScreen
import rfm.hillsongptapp.feature.events.EventsScreen
import rfm.hillsongptapp.feature.home.ui.screens.homeScreen
import rfm.hillsongptapp.logging.LoggerHelper

@Composable
@Preview
fun RootApp() {
    RootNavigation()
}

@Composable
fun RootNavigation() {
    val rootNavController = rememberNavController()
    NavHost(
        navController = rootNavController,
        startDestination = LoginGraph
    ) {
        loginGraph(content = { LoginScreen(navigator = rootNavController) }, rootNavController = rootNavController)
        homeGraph(
            stream = { StreamScreen() },
            settings = { SettingsScreen() },
            profile = { ProfileScreen() },
            ministries = { MinistriesScreen() },
            kids = { KidsScreen(navController = rootNavController) },
            groups = { GroupsScreen() },
            giving = { GivingScreen() },
            feed = { FeedScreen() },
            events = { EventsScreen() },
            homeScreen = { homeScreen(navController = rootNavController) },
            youtubeVideoScreen = { videoId, videoUrl ->
                rfm.hillsongptapp.feature.home.ui.screens.YouTubeVideoScreen(
                    videoId = videoId,
                    videoUrl = videoUrl,
                    navController = rootNavController
                )
            }
        )
        kidsGraph(
            kidsManagement = { 
                KidsManagementScreen(navController = rootNavController)
            },
            kidsRegistration = { 
                ChildRegistrationScreen(
                    onNavigateBack = { rootNavController.popBackStack() },
                    onRegistrationSuccess = { 
                        rootNavController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.Management) {
                            popUpTo(rfm.hillsongptapp.core.navigation.KidsNav.Management) { inclusive = true }
                        }
                    }
                )
            },

            kidsCheckOut = { childId -> 
                CheckOutScreen(
                    childId = childId,
                    onNavigateBack = { rootNavController.popBackStack() }
                )
            },
            kidsEditChild = { childId -> 
                ChildEditScreen(
                    childId = childId,
                    onNavigateBack = { rootNavController.popBackStack() },
                    onUpdateSuccess = { 
                        rootNavController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.Management) {
                            popUpTo(rfm.hillsongptapp.core.navigation.KidsNav.Management) { inclusive = true }
                        }
                    }
                )
            },
            kidsReports = { 
                ReportsScreen(
                    onNavigateBack = { rootNavController.popBackStack() }
                )
            },
            staffDashboard = {
                StaffDashboardScreen(
                    onNavigateBack = { rootNavController.popBackStack() },
                    onNavigateToScanner = {
                        rootNavController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.QRCodeScanner)
                    },
                    onNavigateToCheckInVerification = { token ->
                        rootNavController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.CheckInVerification(token))
                    }
                )
            },
            qrCodeScanner = {
                QRCodeScannerScreen(
                    onNavigateBack = { rootNavController.popBackStack() },
                    onQRCodeScanned = { token ->
                        rootNavController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.CheckInVerification(token))
                    }
                )
            },
            checkInVerification = { token ->
                CheckInVerificationScreen(
                    token = token,
                    onNavigateBack = { rootNavController.popBackStack() }
                )
            },
            qrCodeDisplay = { childId, serviceId ->
                LoggerHelper.logDebug("QRCodeDisplay composable called with childId=$childId, serviceId=$serviceId", "RootApp")
                QRCodeDisplayWrapper(
                    childId = childId,
                    serviceId = serviceId,
                    onNavigateBack = { 
                        LoggerHelper.logDebug("QRCodeDisplay navigating back", "RootApp")
                        // Set flag to trigger refresh in KidsManagementScreen
                        rootNavController.previousBackStackEntry?.savedStateHandle?.set("refresh_key", true)
                        rootNavController.popBackStack() 
                    },
                    onGenerateNewCode = { newChildId, newServiceId ->
                        LoggerHelper.logDebug("Generating new QR code with childId=$newChildId, serviceId=$newServiceId", "RootApp")
                        rootNavController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.QRCodeDisplay(newChildId, newServiceId)) {
                            popUpTo(rfm.hillsongptapp.core.navigation.KidsNav.QRCodeDisplay(childId, serviceId)) { inclusive = true }
                        }
                    }
                )
            },
        )
    }
}