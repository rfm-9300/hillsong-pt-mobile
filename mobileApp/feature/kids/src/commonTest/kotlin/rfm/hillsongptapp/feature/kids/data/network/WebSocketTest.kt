package rfm.hillsongptapp.feature.kids.data.network

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.*
import rfm.hillsongptapp.feature.kids.data.network.websocket.*

class WebSocketTest {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    @Test
    fun `WebSocketMessageFactory creates subscribe to child message correctly`() {
        // Given
        val childId = "child123"
        
        // When
        val message = WebSocketMessageFactory.subscribeToChild(childId)
        
        // Then
        assertEquals("subscribe_child", message.type)
        assertEquals(childId, message.childId)
        assertNotNull(message.timestamp)
    }
    
    @Test
    fun `WebSocketMessageFactory creates subscribe to service message correctly`() {
        // Given
        val serviceId = "service123"
        
        // When
        val message = WebSocketMessageFactory.subscribeToService(serviceId)
        
        // Then
        assertEquals("subscribe_service", message.type)
        assertEquals(serviceId, message.serviceId)
        assertNotNull(message.timestamp)
    }
    
    @Test
    fun `WebSocketMessageFactory creates unsubscribe message correctly`() {
        // When
        val message = WebSocketMessageFactory.unsubscribe()
        
        // Then
        assertEquals("unsubscribe", message.type)
        assertNotNull(message.timestamp)
    }
    
    @Test
    fun `WebSocketMessageFactory creates heartbeat response correctly`() {
        // When
        val message = WebSocketMessageFactory.heartbeatResponse()
        
        // Then
        assertEquals("heartbeat_response", message.type)
        assertNotNull(message.timestamp)
    }
    
    @Test
    fun `ChildStatusUpdateMessage serializes correctly`() {
        // Given
        val message = ChildStatusUpdateMessage(
            child = createTestChildDto(),
            previousStatus = "NOT_IN_SERVICE",
            newStatus = "CHECKED_IN",
            serviceId = "service123",
            timestamp = "2024-01-01T10:00:00Z"
        )
        
        // When
        val serialized = json.encodeToString(ChildStatusUpdateMessage.serializer(), message)
        val deserialized = json.decodeFromString(ChildStatusUpdateMessage.serializer(), serialized)
        
        // Then
        assertEquals(message.type, deserialized.type)
        assertEquals(message.child.id, deserialized.child.id)
        assertEquals(message.previousStatus, deserialized.previousStatus)
        assertEquals(message.newStatus, deserialized.newStatus)
        assertEquals(message.serviceId, deserialized.serviceId)
    }
    
    @Test
    fun `ServiceCapacityUpdateMessage serializes correctly`() {
        // Given
        val message = ServiceCapacityUpdateMessage(
            service = createTestServiceDto(),
            previousCapacity = 5,
            newCapacity = 6,
            timestamp = "2024-01-01T10:00:00Z"
        )
        
        // When
        val serialized = json.encodeToString(ServiceCapacityUpdateMessage.serializer(), message)
        val deserialized = json.decodeFromString(ServiceCapacityUpdateMessage.serializer(), serialized)
        
        // Then
        assertEquals(message.type, deserialized.type)
        assertEquals(message.service.id, deserialized.service.id)
        assertEquals(message.previousCapacity, deserialized.previousCapacity)
        assertEquals(message.newCapacity, deserialized.newCapacity)
    }
    
    @Test
    fun `ErrorMessage serializes correctly`() {
        // Given
        val message = ErrorMessage(
            error = "Test error",
            code = "ERR001",
            details = "Error details",
            timestamp = "2024-01-01T10:00:00Z"
        )
        
        // When
        val serialized = json.encodeToString(ErrorMessage.serializer(), message)
        val deserialized = json.decodeFromString(ErrorMessage.serializer(), serialized)
        
        // Then
        assertEquals(message.type, deserialized.type)
        assertEquals(message.error, deserialized.error)
        assertEquals(message.code, deserialized.code)
        assertEquals(message.details, deserialized.details)
    }
    
    @Test
    fun `HeartbeatMessage serializes correctly`() {
        // Given
        val message = HeartbeatMessage(
            timestamp = "2024-01-01T10:00:00Z"
        )
        
        // When
        val serialized = json.encodeToString(HeartbeatMessage.serializer(), message)
        val deserialized = json.decodeFromString(HeartbeatMessage.serializer(), serialized)
        
        // Then
        assertEquals(message.type, deserialized.type)
        assertEquals(message.timestamp, deserialized.timestamp)
    }
    
    private fun createTestChildDto() = rfm.hillsongptapp.feature.kids.data.network.dto.ChildDto(
        id = "child123",
        parentId = "parent123",
        name = "Test Child",
        dateOfBirth = "2020-01-01",
        emergencyContact = rfm.hillsongptapp.feature.kids.data.network.dto.EmergencyContactDto(
            name = "Emergency Contact",
            phoneNumber = "+1234567890",
            relationship = "Parent"
        ),
        status = "CHECKED_IN",
        currentServiceId = "service123",
        checkInTime = "2024-01-01T10:00:00Z",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T10:00:00Z"
    )
    
    private fun createTestServiceDto() = rfm.hillsongptapp.feature.kids.data.network.dto.KidsServiceDto(
        id = "service123",
        name = "Kids Church",
        description = "Sunday kids service",
        minAge = 3,
        maxAge = 12,
        startTime = "2024-01-01T10:00:00Z",
        endTime = "2024-01-01T11:00:00Z",
        location = "Kids Room",
        maxCapacity = 20,
        currentCapacity = 6,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff1", "staff2"),
        createdAt = "2024-01-01T00:00:00Z"
    )
}