package rfm.hillsongptapp.core.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateToHome() {
    navigate(HomeGraph) {
        // Clear the back stack to prevent returning to the login screen
        popUpTo(LoginGraph) {
            inclusive = true
        }
    }
}

fun NavHostController.navigateToStream() {
    navigate(HomeNav.StreamScreen)
}

fun NavHostController.navigateToSettings() {
    navigate(HomeNav.SettingsScreen)
}

fun NavHostController.navigateToProfile() {
    navigate(HomeNav.ProfileScreen)
}

fun NavHostController.navigateToMinistries() {
    navigate(HomeNav.MinistriesScreen)
}

fun NavHostController.navigateToKids() {
    navigate(HomeNav.KidsScreen)
}

fun NavHostController.navigateToGroups() {
    navigate(HomeNav.GroupsScreen)
}

fun NavHostController.navigateToGiving() {
    navigate(HomeNav.GivingScreen)
}

fun NavHostController.navigateToFeed() {
    navigate(HomeNav.FeedScreen)
}

fun NavHostController.navigateToEvents() {
    navigate(HomeNav.EventsScreen)
}

fun NavHostController.navigateToYouTubeVideo(videoId: Long, videoUrl: String) {
    navigate(HomeNav.YouTubeVideoScreen(videoId, videoUrl))
}

fun NavHostController.navigateToYouTubePlayerFullScreen(videoId: String) {
    navigate(HomeNav.YouTubePlayerFullScreen(videoId))
}

// Kids Management Navigation Extensions
fun NavHostController.navigateToKidsManagement() {
    navigate(KidsNav.Management)
}

fun NavHostController.navigateToKidsRegistration() {
    navigate(KidsNav.Registration)
}

fun NavHostController.navigateToKidsServices() {
    navigate(KidsNav.Services)
}

fun NavHostController.navigateToKidsServicesForChild(childId: String) {
    navigate(KidsNav.ServicesForChild(childId))
}

fun NavHostController.navigateToKidsCheckIn(childId: String) {
    navigate(KidsNav.CheckIn(childId))
}

fun NavHostController.navigateToKidsCheckOut(childId: String) {
    navigate(KidsNav.CheckOut(childId))
}

fun NavHostController.navigateToKidsEditChild(childId: String) {
    navigate(KidsNav.EditChild(childId))
}

fun NavHostController.navigateToKidsReports() {
    navigate(KidsNav.Reports)
}

// Staff Navigation Extensions
fun NavHostController.navigateToStaffDashboard() {
    navigate(KidsNav.StaffDashboard)
}

fun NavHostController.navigateToQRCodeScanner() {
    navigate(KidsNav.QRCodeScanner)
}

fun NavHostController.navigateToCheckInVerification(token: String) {
    navigate(KidsNav.CheckInVerification(token))
}

fun NavHostController.navigateToQRCodeDisplay(childId: Long, serviceId: Long) {
    navigate(KidsNav.QRCodeDisplay(childId, serviceId))
}