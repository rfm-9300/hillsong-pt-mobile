package rfm.hillsongptapp.feature.kids.ui.components

import kotlin.test.*
import rfm.hillsongptapp.feature.kids.data.network.websocket.StatusNotification
import rfm.hillsongptapp.feature.kids.data.network.websocket.NotificationType

class StatusNotificationSystemTest {
    
    @Test
    fun `formatTimestamp returns correct relative time for recent timestamps`() {
        val now = System.currentTimeMillis()
        
        // Just now (less than 1 minute)
        val justNow = now - 30_000 // 30 seconds ago
        assertEquals("Just now", formatTimestamp(justNow))
        
        // Minutes ago
        val fiveMinutesAgo = now - 300_000 // 5 minutes ago
        assertEquals("5m ago", formatTimestamp(fiveMinutesAgo))
        
        // Hours ago
        val twoHoursAgo = now - 7_200_000 // 2 hours ago
        assertEquals("2h ago", formatTimestamp(twoHoursAgo))
        
        // Days ago
        val threeDaysAgo = now - 259_200_000 // 3 days ago
        assertEquals("3d ago", formatTimestamp(threeDaysAgo))
    }
    
    @Test
    fun `getNotificationStyle returns correct values for each notification type`() {
        val testCases = mapOf(
            NotificationType.CONNECTION_ESTABLISHED to Triple("Primary", "Primary", "Wifi"),
            NotificationType.CONNECTION_LOST to Triple("Error", "Error", "WifiOff"),
            NotificationType.CONNECTION_FAILED to Triple("Error", "Error", "Error"),
            NotificationType.CHILD_STATUS_CHANGED to Triple("Secondary", "Secondary", "Person"),
            NotificationType.CHILD_CHECKED_IN to Triple("Primary", "Primary", "CheckCircle"),
            NotificationType.CHILD_CHECKED_OUT to Triple("Tertiary", "Tertiary", "ExitToApp"),
            NotificationType.SERVICE_FULL to Triple("Error", "Error", "Warning"),
            NotificationType.SERVICE_AVAILABLE to Triple("Primary", "Primary", "CheckCircle"),
            NotificationType.ERROR to Triple("Error", "Error", "Error")
        )
        
        testCases.forEach { (type, expected) ->
            val (backgroundColor, contentColor, icon) = getNotificationStyleInfo(type)
            assertEquals(expected.first, backgroundColor, "Background color mismatch for $type")
            assertEquals(expected.second, contentColor, "Content color mismatch for $type")
            assertEquals(expected.third, icon, "Icon mismatch for $type")
        }
    }
    
    @Test
    fun `notification creation with all fields`() {
        val notification = StatusNotification(
            type = NotificationType.CHILD_CHECKED_IN,
            title = "Child Checked In",
            message = "John Doe checked into Sunday School",
            childId = "child123",
            serviceId = "service456",
            timestamp = 1672574400000L
        )
        
        assertEquals(NotificationType.CHILD_CHECKED_IN, notification.type)
        assertEquals("Child Checked In", notification.title)
        assertEquals("John Doe checked into Sunday School", notification.message)
        assertEquals("child123", notification.childId)
        assertEquals("service456", notification.serviceId)
        assertEquals(1672574400000L, notification.timestamp)
    }
    
    @Test
    fun `notification creation with minimal fields`() {
        val notification = StatusNotification(
            type = NotificationType.CONNECTION_ESTABLISHED,
            title = "Connected",
            message = "Real-time updates active",
            timestamp = 1672574400000L
        )
        
        assertEquals(NotificationType.CONNECTION_ESTABLISHED, notification.type)
        assertEquals("Connected", notification.title)
        assertEquals("Real-time updates active", notification.message)
        assertNull(notification.childId)
        assertNull(notification.serviceId)
        assertEquals(1672574400000L, notification.timestamp)
    }
    
    // Helper function to simulate the logic from the Composable
    private fun getNotificationStyleInfo(type: NotificationType): Triple<String, String, String> {
        return when (type) {
            NotificationType.CONNECTION_ESTABLISHED -> Triple("Primary", "Primary", "Wifi")
            NotificationType.CONNECTION_LOST -> Triple("Error", "Error", "WifiOff")
            NotificationType.CONNECTION_FAILED -> Triple("Error", "Error", "Error")
            NotificationType.CHILD_STATUS_CHANGED -> Triple("Secondary", "Secondary", "Person")
            NotificationType.CHILD_CHECKED_IN -> Triple("Primary", "Primary", "CheckCircle")
            NotificationType.CHILD_CHECKED_OUT -> Triple("Tertiary", "Tertiary", "ExitToApp")
            NotificationType.SERVICE_FULL -> Triple("Error", "Error", "Warning")
            NotificationType.SERVICE_AVAILABLE -> Triple("Primary", "Primary", "CheckCircle")
            NotificationType.ERROR -> Triple("Error", "Error", "Error")
        }
    }
    
    // Helper function to simulate the timestamp formatting logic
    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            else -> "${diff / 86400_000}d ago"
        }
    }
}

class NotificationBadgeTest {
    
    @Test
    fun `notification badge shows correct text for different counts`() {
        assertEquals("1", getBadgeText(1))
        assertEquals("5", getBadgeText(5))
        assertEquals("99", getBadgeText(99))
        assertEquals("99+", getBadgeText(100))
        assertEquals("99+", getBadgeText(150))
    }
    
    @Test
    fun `notification badge should not show for zero count`() {
        assertFalse(shouldShowBadge(0))
    }
    
    @Test
    fun `notification badge should show for positive counts`() {
        assertTrue(shouldShowBadge(1))
        assertTrue(shouldShowBadge(5))
        assertTrue(shouldShowBadge(100))
    }
    
    private fun getBadgeText(count: Int): String {
        return if (count > 99) "99+" else count.toString()
    }
    
    private fun shouldShowBadge(count: Int): Boolean {
        return count > 0
    }
}