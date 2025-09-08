package rfm.hillsongptapp.feature.kids.domain.offline

import kotlinx.coroutines.test.runTest
import co.touchlab.kermit.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfflineHandlerTest {
    
    private val logger = Logger.withTag("OfflineHandlerTest")
    private val offlineHandler = OfflineHandler(logger)
    
    @Test
    fun `setOfflineStatus should update offline status and capabilities`() = runTest {
        // Initially online
        assertFalse(offlineHandler.isOffline.value)
        
        // Set offline
        offlineHandler.setOfflineStatus(true)
        assertTrue(offlineHandler.isOffline.value)
        
        val capabilities = offlineHandler.offlineCapabilities.value
        assertTrue(capabilities.canViewChildren)
        assertTrue(capabilities.canViewServices)
        assertFalse(capabilities.canEditChildInfo)
        assertFalse(capabilities.canCheckIn)
        assertFalse(capabilities.canCheckOut)
        assertFalse(capabilities.canRegisterChild)
        assertTrue(capabilities.canViewReports)
        assertFalse(capabilities.hasRealTimeUpdates)
        
        // Set back online
        offlineHandler.setOfflineStatus(false)
        assertFalse(offlineHandler.isOffline.value)
    }
    
    @Test
    fun `isOperationAvailableOffline should return correct availability`() {
        offlineHandler.setOfflineStatus(true)
        
        assertTrue(offlineHandler.isOperationAvailableOffline(OfflineOperation.VIEW_CHILDREN))
        assertTrue(offlineHandler.isOperationAvailableOffline(OfflineOperation.VIEW_SERVICES))
        assertFalse(offlineHandler.isOperationAvailableOffline(OfflineOperation.EDIT_CHILD))
        assertFalse(offlineHandler.isOperationAvailableOffline(OfflineOperation.CHECK_IN))
        assertFalse(offlineHandler.isOperationAvailableOffline(OfflineOperation.CHECK_OUT))
        assertFalse(offlineHandler.isOperationAvailableOffline(OfflineOperation.REGISTER_CHILD))
        assertTrue(offlineHandler.isOperationAvailableOffline(OfflineOperation.VIEW_REPORTS))
    }
    
    @Test
    fun `getOfflineMessage should return appropriate messages for each operation`() {
        val editMessage = offlineHandler.getOfflineMessage(OfflineOperation.EDIT_CHILD)
        assertTrue(editMessage.contains("cannot be edited while offline"))
        
        val checkInMessage = offlineHandler.getOfflineMessage(OfflineOperation.CHECK_IN)
        assertTrue(checkInMessage.contains("not available offline for safety reasons"))
        
        val checkOutMessage = offlineHandler.getOfflineMessage(OfflineOperation.CHECK_OUT)
        assertTrue(checkOutMessage.contains("not available offline for safety reasons"))
        
        val registerMessage = offlineHandler.getOfflineMessage(OfflineOperation.REGISTER_CHILD)
        assertTrue(registerMessage.contains("requires an internet connection"))
        
        val viewChildrenMessage = offlineHandler.getOfflineMessage(OfflineOperation.VIEW_CHILDREN)
        assertTrue(viewChildrenMessage.contains("not available offline"))
        
        val viewServicesMessage = offlineHandler.getOfflineMessage(OfflineOperation.VIEW_SERVICES)
        assertTrue(viewServicesMessage.contains("not available offline"))
        
        val viewReportsMessage = offlineHandler.getOfflineMessage(OfflineOperation.VIEW_REPORTS)
        assertTrue(viewReportsMessage.contains("not available offline"))
    }
    
    @Test
    fun `addPendingOperation should add operation to pending list`() = runTest {
        val operation = PendingOperation(
            id = "op1",
            type = PendingOperationType.REGISTER_CHILD,
            data = mapOf("childName" to "John Doe"),
            timestamp = System.currentTimeMillis(),
            description = "Register child John Doe"
        )
        
        assertEquals(0, offlineHandler.pendingOperations.value.size)
        
        offlineHandler.addPendingOperation(operation)
        
        assertEquals(1, offlineHandler.pendingOperations.value.size)
        assertEquals(operation, offlineHandler.pendingOperations.value.first())
    }
    
    @Test
    fun `removePendingOperation should remove operation from pending list`() = runTest {
        val operation1 = PendingOperation(
            id = "op1",
            type = PendingOperationType.REGISTER_CHILD,
            data = mapOf("childName" to "John Doe"),
            timestamp = System.currentTimeMillis(),
            description = "Register child John Doe"
        )
        
        val operation2 = PendingOperation(
            id = "op2",
            type = PendingOperationType.CHECK_IN_CHILD,
            data = mapOf("childId" to "child123"),
            timestamp = System.currentTimeMillis(),
            description = "Check in child"
        )
        
        offlineHandler.addPendingOperation(operation1)
        offlineHandler.addPendingOperation(operation2)
        assertEquals(2, offlineHandler.pendingOperations.value.size)
        
        offlineHandler.removePendingOperation("op1")
        assertEquals(1, offlineHandler.pendingOperations.value.size)
        assertEquals("op2", offlineHandler.pendingOperations.value.first().id)
        
        offlineHandler.removePendingOperation("op2")
        assertEquals(0, offlineHandler.pendingOperations.value.size)
    }
    
    @Test
    fun `clearPendingOperations should remove all pending operations`() = runTest {
        val operation1 = PendingOperation(
            id = "op1",
            type = PendingOperationType.REGISTER_CHILD,
            data = mapOf("childName" to "John Doe"),
            timestamp = System.currentTimeMillis(),
            description = "Register child John Doe"
        )
        
        val operation2 = PendingOperation(
            id = "op2",
            type = PendingOperationType.CHECK_IN_CHILD,
            data = mapOf("childId" to "child123"),
            timestamp = System.currentTimeMillis(),
            description = "Check in child"
        )
        
        offlineHandler.addPendingOperation(operation1)
        offlineHandler.addPendingOperation(operation2)
        assertEquals(2, offlineHandler.pendingOperations.value.size)
        
        offlineHandler.clearPendingOperations()
        assertEquals(0, offlineHandler.pendingOperations.value.size)
    }
    
    @Test
    fun `getFallbackDataMessage should return appropriate message`() {
        val message = offlineHandler.getFallbackDataMessage()
        assertTrue(message.contains("cached information"))
        assertTrue(message.contains("may be outdated"))
    }
    
    @Test
    fun `getReconnectionMessage should return appropriate message based on pending operations`() {
        // No pending operations
        val messageNoPending = offlineHandler.getReconnectionMessage()
        assertTrue(messageNoPending.contains("Connection restored"))
        assertTrue(messageNoPending.contains("All features are now available"))
        
        // With pending operations
        val operation = PendingOperation(
            id = "op1",
            type = PendingOperationType.REGISTER_CHILD,
            data = mapOf("childName" to "John Doe"),
            timestamp = System.currentTimeMillis(),
            description = "Register child John Doe"
        )
        offlineHandler.addPendingOperation(operation)
        
        val messageWithPending = offlineHandler.getReconnectionMessage()
        assertTrue(messageWithPending.contains("Connection restored"))
        assertTrue(messageWithPending.contains("Syncing 1 pending operation"))
        
        // Multiple pending operations
        val operation2 = PendingOperation(
            id = "op2",
            type = PendingOperationType.CHECK_IN_CHILD,
            data = mapOf("childId" to "child123"),
            timestamp = System.currentTimeMillis(),
            description = "Check in child"
        )
        offlineHandler.addPendingOperation(operation2)
        
        val messageMultiplePending = offlineHandler.getReconnectionMessage()
        assertTrue(messageMultiplePending.contains("Syncing 2 pending operations"))
    }
    
    @Test
    fun `getOfflineStatusMessage should return appropriate message`() {
        val message = offlineHandler.getOfflineStatusMessage()
        assertTrue(message.contains("currently offline"))
        assertTrue(message.contains("Some features are limited"))
    }
    
    @Test
    fun `isCachedDataStale should correctly determine staleness`() {
        val currentTime = System.currentTimeMillis()
        val recentTime = currentTime - 60_000L // 1 minute ago
        val staleTime = currentTime - 600_000L // 10 minutes ago
        
        assertFalse(offlineHandler.isCachedDataStale(recentTime, 300_000L)) // 5 minute threshold
        assertTrue(offlineHandler.isCachedDataStale(staleTime, 300_000L)) // 5 minute threshold
    }
    
    @Test
    fun `getStaleDataWarning should return appropriate warning message`() {
        val currentTime = System.currentTimeMillis()
        val fiveMinutesAgo = currentTime - 300_000L
        
        val warning = offlineHandler.getStaleDataWarning(fiveMinutesAgo)
        assertTrue(warning.contains("5 minutes old"))
        assertTrue(warning.contains("Connect to refresh"))
    }
    
    @Test
    fun `getOfflineFeatureLimitations should return list of limitations`() {
        val limitations = offlineHandler.getOfflineFeatureLimitations()
        
        assertTrue(limitations.isNotEmpty())
        assertTrue(limitations.any { it.contains("Check-in and check-out operations are disabled") })
        assertTrue(limitations.any { it.contains("Child registration and editing require internet") })
        assertTrue(limitations.any { it.contains("Real-time status updates are not available") })
        assertTrue(limitations.any { it.contains("Service capacity information may be outdated") })
        assertTrue(limitations.any { it.contains("Reports show cached data only") })
    }
    
    @Test
    fun `getOfflineRecoverySuggestions should return list of suggestions`() {
        val suggestions = offlineHandler.getOfflineRecoverySuggestions()
        
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.any { it.contains("Check your WiFi or mobile data") })
        assertTrue(suggestions.any { it.contains("Move to an area with better signal") })
        assertTrue(suggestions.any { it.contains("Restart your internet connection") })
        assertTrue(suggestions.any { it.contains("Contact church staff") })
        assertTrue(suggestions.any { it.contains("Try again when connection is restored") })
    }
}