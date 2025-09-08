package rfm.hillsongptapp.feature.kids.data.network.datasource

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import rfm.hillsongptapp.feature.kids.data.network.dto.*
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.data.network.error.toKidsManagementError
import rfm.hillsongptapp.feature.kids.data.network.websocket.*

/**
 * Implementation of KidsRemoteDataSource using Ktor HTTP client
 * Handles all network communication for kids management operations
 */
class KidsRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val json: Json,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : KidsRemoteDataSource {
    
    private var webSocketSession: DefaultClientWebSocketSession? = null
    private val webSocketMessages = Channel<WebSocketMessage>(Channel.UNLIMITED)
    
    companion object {
        private const val API_VERSION = "v1"
        private const val KIDS_ENDPOINT = "kids"
        private const val SERVICES_ENDPOINT = "services"
        private const val CHECKIN_ENDPOINT = "checkin"
        private const val REPORTS_ENDPOINT = "reports"
        private const val WEBSOCKET_ENDPOINT = "ws/kids"
    }
    
    // Child Management Operations
    
    override suspend fun getChildrenForParent(parentId: String): Result<ChildrenResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/$API_VERSION/$KIDS_ENDPOINT/parent/$parentId") {
                contentType(ContentType.Application.Json)
            }.body<ChildrenResponse>()
        }
    }
    
    override suspend fun registerChild(request: ChildRegistrationRequest): Result<ChildResponse> {
        return safeApiCall {
            httpClient.post("$baseUrl/$API_VERSION/$KIDS_ENDPOINT") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<ChildResponse>()
        }
    }
    
    override suspend fun updateChild(childId: String, request: ChildUpdateRequest): Result<ChildResponse> {
        return safeApiCall {
            httpClient.put("$baseUrl/$API_VERSION/$KIDS_ENDPOINT/$childId") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<ChildResponse>()
        }
    }
    
    override suspend fun deleteChild(childId: String): Result<Unit> {
        return safeApiCall {
            val response = httpClient.delete("$baseUrl/$API_VERSION/$KIDS_ENDPOINT/$childId") {
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) {
                Unit
            } else {
                throw KidsManagementError.ApiError(response.status.value, response.bodyAsText())
            }
        }
    }
    
    override suspend fun getChildById(childId: String): Result<ChildResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/$API_VERSION/$KIDS_ENDPOINT/$childId") {
                contentType(ContentType.Application.Json)
            }.body<ChildResponse>()
        }
    }
    
    // Service Management Operations
    
    override suspend fun getAvailableServices(): Result<ServicesResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/$API_VERSION/$SERVICES_ENDPOINT") {
                contentType(ContentType.Application.Json)
            }.body<ServicesResponse>()
        }
    }
    
    override suspend fun getServicesForAge(age: Int): Result<ServicesResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/$API_VERSION/$SERVICES_ENDPOINT") {
                contentType(ContentType.Application.Json)
                parameter("age", age)
            }.body<ServicesResponse>()
        }
    }
    
    override suspend fun getServiceById(serviceId: String): Result<ServiceResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/$API_VERSION/$SERVICES_ENDPOINT/$serviceId") {
                contentType(ContentType.Application.Json)
            }.body<ServiceResponse>()
        }
    }
    
    override suspend fun getServicesAcceptingCheckIns(): Result<ServicesResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/$API_VERSION/$SERVICES_ENDPOINT") {
                contentType(ContentType.Application.Json)
                parameter("accepting_checkins", true)
            }.body<ServicesResponse>()
        }
    }
    
    // Check-in/Check-out Operations
    
    override suspend fun checkInChild(request: CheckInRequest): Result<CheckInResponse> {
        return safeApiCall {
            httpClient.post("$baseUrl/$API_VERSION/$CHECKIN_ENDPOINT") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<CheckInResponse>()
        }
    }
    
    override suspend fun checkOutChild(request: CheckOutRequest): Result<CheckOutResponse> {
        return safeApiCall {
            httpClient.post("$baseUrl/$API_VERSION/$CHECKIN_ENDPOINT/checkout") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<CheckOutResponse>()
        }
    }
    
    override suspend fun getCheckInHistory(childId: String, limit: Int?): Result<CheckInHistoryResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/$API_VERSION/$CHECKIN_ENDPOINT/history/$childId") {
                contentType(ContentType.Application.Json)
                limit?.let { parameter("limit", it) }
            }.body<CheckInHistoryResponse>()
        }
    }
    
    override suspend fun getCurrentCheckIns(serviceId: String): Result<CurrentCheckInsResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/$API_VERSION/$CHECKIN_ENDPOINT/current/service/$serviceId") {
                contentType(ContentType.Application.Json)
            }.body<CurrentCheckInsResponse>()
        }
    }
    
    override suspend fun getAllCurrentCheckIns(): Result<CurrentCheckInsResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/$API_VERSION/$CHECKIN_ENDPOINT/current") {
                contentType(ContentType.Application.Json)
            }.body<CurrentCheckInsResponse>()
        }
    }
    
    // Staff/Reporting Operations
    
    override suspend fun getServiceReport(serviceId: String): Result<ServiceReportResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/$API_VERSION/$REPORTS_ENDPOINT/service/$serviceId") {
                contentType(ContentType.Application.Json)
            }.body<ServiceReportResponse>()
        }
    }
    
    override suspend fun getAttendanceReport(request: AttendanceReportRequest): Result<AttendanceReportResponse> {
        return safeApiCall {
            httpClient.post("$baseUrl/$API_VERSION/$REPORTS_ENDPOINT/attendance") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<AttendanceReportResponse>()
        }
    }
    
    // Real-time WebSocket Operations
    
    override suspend fun connectWebSocket(): Result<Unit> {
        return try {
            webSocketSession = httpClient.webSocketSession(
                method = HttpMethod.Get,
                host = extractHost(baseUrl),
                port = extractPort(baseUrl),
                path = "/$WEBSOCKET_ENDPOINT"
            )
            
            // Start listening for messages
            webSocketSession?.let { session ->
                coroutineScope.launch {
                    try {
                        for (frame in session.incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val messageText = frame.readText()
                                    try {
                                        val message = parseWebSocketMessage(messageText)
                                        webSocketMessages.trySend(message)
                                    } catch (e: Exception) {
                                        // Handle parsing error
                                        val errorMessage = UnknownMessage(
                                            data = messageText,
                                            timestamp = System.currentTimeMillis().toString()
                                        )
                                        webSocketMessages.trySend(errorMessage)
                                    }
                                }
                                is Frame.Close -> {
                                    val errorMessage = ErrorMessage(
                                        error = "WebSocket connection closed",
                                        code = frame.readReason()?.code?.toString(),
                                        details = frame.readReason()?.message,
                                        timestamp = System.currentTimeMillis().toString()
                                    )
                                    webSocketMessages.trySend(errorMessage)
                                    break
                                }
                                else -> {
                                    // Handle other frame types if needed
                                }
                            }
                        }
                    } catch (e: Exception) {
                        val errorMessage = ErrorMessage(
                            error = "WebSocket error: ${e.message}",
                            timestamp = System.currentTimeMillis().toString()
                        )
                        webSocketMessages.trySend(errorMessage)
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(KidsManagementError.WebSocketConnectionFailed)
        }
    }
    
    override suspend fun disconnectWebSocket() {
        try {
            webSocketSession?.close()
            webSocketSession = null
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }
    
    override suspend fun subscribeToChildUpdates(childId: String): Result<Unit> {
        return sendWebSocketMessage(WebSocketMessageFactory.subscribeToChild(childId))
    }
    
    override suspend fun subscribeToServiceUpdates(serviceId: String): Result<Unit> {
        return sendWebSocketMessage(WebSocketMessageFactory.subscribeToService(serviceId))
    }
    
    override suspend fun unsubscribeFromUpdates(): Result<Unit> {
        return sendWebSocketMessage(WebSocketMessageFactory.unsubscribe())
    }
    
    override fun getWebSocketMessages(): Flow<WebSocketMessage> {
        return webSocketMessages.receiveAsFlow()
    }
    
    override fun isWebSocketConnected(): Boolean {
        return webSocketSession?.isActive == true
    }
    
    // Private helper methods
    
    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: ClientRequestException) {
            val errorMessage = try {
                e.response.bodyAsText()
            } catch (ex: Exception) {
                e.message ?: "Unknown client error"
            }
            Result.failure(e.response.status.value.toKidsManagementError(errorMessage))
        } catch (e: ServerResponseException) {
            val errorMessage = try {
                e.response.bodyAsText()
            } catch (ex: Exception) {
                e.message ?: "Unknown server error"
            }
            Result.failure(e.response.status.value.toKidsManagementError(errorMessage))
        } catch (e: HttpRequestTimeoutException) {
            Result.failure(KidsManagementError.TimeoutError)
        } catch (e: Exception) {
            Result.failure(KidsManagementError.NetworkError)
        }
    }
    
    private suspend fun sendWebSocketMessage(message: WebSocketMessage): Result<Unit> {
        return try {
            val session = webSocketSession ?: return Result.failure(KidsManagementError.WebSocketConnectionFailed)
            val messageJson = json.encodeToString(message)
            session.send(Frame.Text(messageJson))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(KidsManagementError.WebSocketConnectionFailed)
        }
    }
    
    private fun parseWebSocketMessage(messageText: String): WebSocketMessage {
        // This is a simplified parser - in a real implementation, you'd have more robust parsing
        return try {
            // Try to determine message type from JSON
            val jsonElement = json.parseToJsonElement(messageText)
            val type = jsonElement.jsonObject["type"]?.jsonPrimitive?.content ?: "unknown"
            
            when (type) {
                "child_status_update" -> json.decodeFromString<ChildStatusUpdateMessage>(messageText)
                "service_capacity_update" -> json.decodeFromString<ServiceCapacityUpdateMessage>(messageText)
                "check_in_update" -> json.decodeFromString<CheckInUpdateMessage>(messageText)
                "check_out_update" -> json.decodeFromString<CheckOutUpdateMessage>(messageText)
                "connection_established" -> json.decodeFromString<ConnectionEstablishedMessage>(messageText)
                "heartbeat" -> json.decodeFromString<HeartbeatMessage>(messageText)
                "error" -> json.decodeFromString<ErrorMessage>(messageText)
                else -> UnknownMessage(
                    data = messageText,
                    timestamp = System.currentTimeMillis().toString()
                )
            }
        } catch (e: Exception) {
            UnknownMessage(
                data = messageText,
                timestamp = System.currentTimeMillis().toString()
            )
        }
    }
    
    private fun extractHost(url: String): String {
        return try {
            val cleanUrl = url.removePrefix("http://").removePrefix("https://")
            cleanUrl.split(":")[0].split("/")[0]
        } catch (e: Exception) {
            "localhost"
        }
    }
    
    private fun extractPort(url: String): Int {
        return try {
            val cleanUrl = url.removePrefix("http://").removePrefix("https://")
            val parts = cleanUrl.split(":")
            if (parts.size > 1) {
                parts[1].split("/")[0].toInt()
            } else {
                if (url.startsWith("https://")) 443 else 80
            }
        } catch (e: Exception) {
            8080
        }
    }
}