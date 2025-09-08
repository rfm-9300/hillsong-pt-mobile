package rfm.hillsongptapp.feature.kids.data.network

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.test.*
import rfm.hillsongptapp.feature.kids.data.network.datasource.KidsRemoteDataSourceImpl
import rfm.hillsongptapp.feature.kids.data.network.dto.*
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError

class KidsRemoteDataSourceTest {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private fun createMockClient(responseContent: String, statusCode: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = ByteReadChannel(responseContent),
                        status = statusCode,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json(json)
            }
            install(WebSockets)
        }
    }
    
    @Test
    fun `getChildrenForParent returns success response`() = runTest {
        // Given
        val parentId = "parent123"
        val mockResponse = ChildrenResponse(
            success = true,
            children = listOf(
                ChildDto(
                    id = "child123",
                    parentId = parentId,
                    name = "Test Child",
                    dateOfBirth = "2020-01-01",
                    emergencyContact = EmergencyContactDto(
                        name = "Emergency Contact",
                        phoneNumber = "+1234567890",
                        relationship = "Parent"
                    ),
                    status = "NOT_IN_SERVICE",
                    createdAt = "2024-01-01T00:00:00Z",
                    updatedAt = "2024-01-01T00:00:00Z"
                )
            )
        )
        val mockClient = createMockClient(json.encodeToString(ChildrenResponse.serializer(), mockResponse))
        val dataSource = KidsRemoteDataSourceImpl(mockClient, "http://localhost:8080", json)
        
        // When
        val result = dataSource.getChildrenForParent(parentId)
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response.success)
        assertEquals(1, response.children.size)
        assertEquals("child123", response.children[0].id)
        assertEquals(parentId, response.children[0].parentId)
    }
    
    @Test
    fun `registerChild returns success response`() = runTest {
        // Given
        val request = ChildRegistrationRequest(
            name = "New Child",
            dateOfBirth = "2021-01-01",
            emergencyContact = EmergencyContactDto(
                name = "Emergency Contact",
                phoneNumber = "+1234567890",
                relationship = "Parent"
            )
        )
        val mockResponse = ChildResponse(
            success = true,
            child = ChildDto(
                id = "newchild123",
                parentId = "parent123",
                name = request.name,
                dateOfBirth = request.dateOfBirth,
                emergencyContact = request.emergencyContact,
                status = "NOT_IN_SERVICE",
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            )
        )
        val mockClient = createMockClient(json.encodeToString(ChildResponse.serializer(), mockResponse))
        val dataSource = KidsRemoteDataSourceImpl(mockClient, "http://localhost:8080", json)
        
        // When
        val result = dataSource.registerChild(request)
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response.success)
        assertNotNull(response.child)
        assertEquals("newchild123", response.child!!.id)
        assertEquals(request.name, response.child!!.name)
    }
    
    @Test
    fun `getAvailableServices returns success response`() = runTest {
        // Given
        val mockResponse = ServicesResponse(
            success = true,
            services = listOf(
                KidsServiceDto(
                    id = "service123",
                    name = "Kids Church",
                    description = "Sunday kids service",
                    minAge = 3,
                    maxAge = 12,
                    startTime = "2024-01-01T10:00:00Z",
                    endTime = "2024-01-01T11:00:00Z",
                    location = "Kids Room",
                    maxCapacity = 20,
                    currentCapacity = 5,
                    isAcceptingCheckIns = true,
                    staffMembers = listOf("staff1", "staff2"),
                    createdAt = "2024-01-01T00:00:00Z"
                )
            )
        )
        val mockClient = createMockClient(json.encodeToString(ServicesResponse.serializer(), mockResponse))
        val dataSource = KidsRemoteDataSourceImpl(mockClient, "http://localhost:8080", json)
        
        // When
        val result = dataSource.getAvailableServices()
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response.success)
        assertEquals(1, response.services.size)
        assertEquals("service123", response.services[0].id)
        assertEquals("Kids Church", response.services[0].name)
    }
    
    @Test
    fun `checkInChild returns success response`() = runTest {
        // Given
        val request = CheckInRequest(
            childId = "child123",
            serviceId = "service123",
            notes = "Test check-in"
        )
        val mockResponse = CheckInResponse(
            success = true,
            record = CheckInRecordDto(
                id = "record123",
                childId = request.childId,
                serviceId = request.serviceId,
                checkInTime = "2024-01-01T10:00:00Z",
                checkedInBy = "user123",
                notes = request.notes,
                status = "CHECKED_IN"
            )
        )
        val mockClient = createMockClient(json.encodeToString(CheckInResponse.serializer(), mockResponse))
        val dataSource = KidsRemoteDataSourceImpl(mockClient, "http://localhost:8080", json)
        
        // When
        val result = dataSource.checkInChild(request)
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response.success)
        assertNotNull(response.record)
        assertEquals("record123", response.record!!.id)
        assertEquals(request.childId, response.record!!.childId)
        assertEquals(request.serviceId, response.record!!.serviceId)
    }
    
    @Test
    fun `checkOutChild returns success response`() = runTest {
        // Given
        val request = CheckOutRequest(
            childId = "child123",
            notes = "Test check-out"
        )
        val mockResponse = CheckOutResponse(
            success = true,
            record = CheckInRecordDto(
                id = "record123",
                childId = request.childId,
                serviceId = "service123",
                checkInTime = "2024-01-01T10:00:00Z",
                checkOutTime = "2024-01-01T11:00:00Z",
                checkedInBy = "user123",
                checkedOutBy = "user123",
                notes = request.notes,
                status = "CHECKED_OUT"
            )
        )
        val mockClient = createMockClient(json.encodeToString(CheckOutResponse.serializer(), mockResponse))
        val dataSource = KidsRemoteDataSourceImpl(mockClient, "http://localhost:8080", json)
        
        // When
        val result = dataSource.checkOutChild(request)
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response.success)
        assertNotNull(response.record)
        assertEquals("record123", response.record!!.id)
        assertEquals(request.childId, response.record!!.childId)
        assertNotNull(response.record!!.checkOutTime)
    }
    
    @Test
    fun `getServiceReport returns success response`() = runTest {
        // Given
        val serviceId = "service123"
        val mockResponse = ServiceReportResponse(
            success = true,
            report = ServiceReportDto(
                serviceId = serviceId,
                serviceName = "Kids Church",
                totalCapacity = 20,
                currentCheckIns = 5,
                availableSpots = 15,
                checkedInChildren = emptyList(),
                staffMembers = listOf("staff1", "staff2"),
                generatedAt = "2024-01-01T12:00:00Z"
            )
        )
        val mockClient = createMockClient(json.encodeToString(ServiceReportResponse.serializer(), mockResponse))
        val dataSource = KidsRemoteDataSourceImpl(mockClient, "http://localhost:8080", json)
        
        // When
        val result = dataSource.getServiceReport(serviceId)
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response.success)
        assertNotNull(response.report)
        assertEquals(serviceId, response.report!!.serviceId)
        assertEquals(20, response.report!!.totalCapacity)
        assertEquals(5, response.report!!.currentCheckIns)
    }
    
    @Test
    fun `API call handles 404 error correctly`() = runTest {
        // Given
        val mockClient = createMockClient("Child not found", HttpStatusCode.NotFound)
        val dataSource = KidsRemoteDataSourceImpl(mockClient, "http://localhost:8080", json)
        
        // When
        val result = dataSource.getChildById("nonexistent")
        
        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is KidsManagementError.ChildNotFound)
    }
    
    @Test
    fun `API call handles 400 validation error correctly`() = runTest {
        // Given
        val mockClient = createMockClient("Invalid request data", HttpStatusCode.BadRequest)
        val dataSource = KidsRemoteDataSourceImpl(mockClient, "http://localhost:8080", json)
        
        // When
        val result = dataSource.registerChild(
            ChildRegistrationRequest(
                name = "",
                dateOfBirth = "invalid-date",
                emergencyContact = EmergencyContactDto("", "", "")
            )
        )
        
        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is KidsManagementError.ValidationError)
    }
    
    @Test
    fun `API call handles 500 server error correctly`() = runTest {
        // Given
        val mockClient = createMockClient("Internal server error", HttpStatusCode.InternalServerError)
        val dataSource = KidsRemoteDataSourceImpl(mockClient, "http://localhost:8080", json)
        
        // When
        val result = dataSource.getAvailableServices()
        
        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is KidsManagementError.ServerError)
    }
    
    @Test
    fun `deleteChild handles successful deletion`() = runTest {
        // Given
        val mockClient = createMockClient("", HttpStatusCode.NoContent)
        val dataSource = KidsRemoteDataSourceImpl(mockClient, "http://localhost:8080", json)
        
        // When
        val result = dataSource.deleteChild("child123")
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `getServicesForAge includes age parameter`() = runTest {
        // Given
        val age = 8
        val mockResponse = ServicesResponse(
            success = true,
            services = listOf(
                KidsServiceDto(
                    id = "service123",
                    name = "Kids Church",
                    description = "Sunday kids service",
                    minAge = 5,
                    maxAge = 10,
                    startTime = "2024-01-01T10:00:00Z",
                    endTime = "2024-01-01T11:00:00Z",
                    location = "Kids Room",
                    maxCapacity = 20,
                    currentCapacity = 5,
                    isAcceptingCheckIns = true,
                    staffMembers = listOf("staff1"),
                    createdAt = "2024-01-01T00:00:00Z"
                )
            )
        )
        val mockClient = createMockClient(json.encodeToString(ServicesResponse.serializer(), mockResponse))
        val dataSource = KidsRemoteDataSourceImpl(mockClient, "http://localhost:8080", json)
        
        // When
        val result = dataSource.getServicesForAge(age)
        
        // Then
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response.success)
        assertEquals(1, response.services.size)
        // Verify the service age range includes the requested age
        val service = response.services[0]
        assertTrue(age >= service.minAge && age <= service.maxAge)
    }
}