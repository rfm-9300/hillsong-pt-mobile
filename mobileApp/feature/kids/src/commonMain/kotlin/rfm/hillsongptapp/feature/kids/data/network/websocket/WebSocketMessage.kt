package rfm.hillsongptapp.feature.kids.data.network.websocket

import kotlinx.serialization.Serializable
import rfm.hillsongptapp.feature.kids.data.network.dto.ChildDto
import rfm.hillsongptapp.feature.kids.data.network.dto.CheckInRecordDto
import rfm.hillsongptapp.feature.kids.data.network.dto.KidsServiceDto

/**
 * Base interface for all WebSocket messages
 */
interface WebSocketMessage {
    val type: String
    val timestamp: String
}

/**
 * Message sent to subscribe to updates for a specific child
 */
@Serializable
data class SubscribeToChildMessage(
    val childId: String,
    override val type: String = "subscribe_child",
    override val timestamp: String
) : WebSocketMessage

/**
 * Message sent to subscribe to updates for a specific service
 */
@Serializable
data class SubscribeToServiceMessage(
    val serviceId: String,
    override val type: String = "subscribe_service",
    override val timestamp: String
) : WebSocketMessage

/**
 * Message sent to unsubscribe from all updates
 */
@Serializable
data class UnsubscribeMessage(
    override val type: String = "unsubscribe",
    override val timestamp: String
) : WebSocketMessage

/**
 * Message received when a child's status is updated
 */
@Serializable
data class ChildStatusUpdateMessage(
    val child: ChildDto,
    val previousStatus: String,
    val newStatus: String,
    val serviceId: String? = null,
    override val type: String = "child_status_update",
    override val timestamp: String
) : WebSocketMessage

/**
 * Message received when a service's capacity is updated
 */
@Serializable
data class ServiceCapacityUpdateMessage(
    val service: KidsServiceDto,
    val previousCapacity: Int,
    val newCapacity: Int,
    override val type: String = "service_capacity_update",
    override val timestamp: String
) : WebSocketMessage

/**
 * Message received when a check-in occurs
 */
@Serializable
data class CheckInUpdateMessage(
    val record: CheckInRecordDto,
    val child: ChildDto,
    val service: KidsServiceDto,
    override val type: String = "check_in_update",
    override val timestamp: String
) : WebSocketMessage

/**
 * Message received when a check-out occurs
 */
@Serializable
data class CheckOutUpdateMessage(
    val record: CheckInRecordDto,
    val child: ChildDto,
    val service: KidsServiceDto,
    override val type: String = "check_out_update",
    override val timestamp: String
) : WebSocketMessage

/**
 * Message received when connection is established
 */
@Serializable
data class ConnectionEstablishedMessage(
    val sessionId: String,
    val userId: String,
    override val type: String = "connection_established",
    override val timestamp: String
) : WebSocketMessage

/**
 * Message received for connection heartbeat/ping
 */
@Serializable
data class HeartbeatMessage(
    override val type: String = "heartbeat",
    override val timestamp: String
) : WebSocketMessage

/**
 * Message sent as heartbeat response/pong
 */
@Serializable
data class HeartbeatResponseMessage(
    override val type: String = "heartbeat_response",
    override val timestamp: String
) : WebSocketMessage

/**
 * Message received when an error occurs
 */
@Serializable
data class ErrorMessage(
    val error: String,
    val code: String? = null,
    val details: String? = null,
    override val type: String = "error",
    override val timestamp: String
) : WebSocketMessage

/**
 * Generic message wrapper for unknown message types
 */
@Serializable
data class UnknownMessage(
    val data: String,
    override val type: String = "unknown",
    override val timestamp: String
) : WebSocketMessage

/**
 * Utility object for creating WebSocket messages with current timestamp
 */
object WebSocketMessageFactory {
    
    fun subscribeToChild(childId: String): SubscribeToChildMessage {
        return SubscribeToChildMessage(
            childId = childId,
            timestamp = getCurrentTimestamp()
        )
    }
    
    fun subscribeToService(serviceId: String): SubscribeToServiceMessage {
        return SubscribeToServiceMessage(
            serviceId = serviceId,
            timestamp = getCurrentTimestamp()
        )
    }
    
    fun unsubscribe(): UnsubscribeMessage {
        return UnsubscribeMessage(
            timestamp = getCurrentTimestamp()
        )
    }
    
    fun heartbeatResponse(): HeartbeatResponseMessage {
        return HeartbeatResponseMessage(
            timestamp = getCurrentTimestamp()
        )
    }
    
    private fun getCurrentTimestamp(): String {
        // In a real implementation, use kotlinx-datetime
        return System.currentTimeMillis().toString()
    }
}