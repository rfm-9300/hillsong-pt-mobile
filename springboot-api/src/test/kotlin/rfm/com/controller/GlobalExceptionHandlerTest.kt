package rfm.com.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.web.bind.annotation.*
import rfm.com.exception.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Email

/**
 * Test controller for testing exception handling
 */
@RestController
@RequestMapping("/test")
class TestController {
    
    @GetMapping("/entity-not-found")
    fun entityNotFound(): String {
        throw EntityNotFoundException("User", 123)
    }
    
    @GetMapping("/unauthorized-action")
    fun unauthorizedAction(): String {
        throw UnauthorizedActionException("delete user", "insufficient permissions")
    }
    
    @GetMapping("/business-rule-violation")
    fun businessRuleViolation(): String {
        throw BusinessRuleViolationException("maximum capacity", "event is full")
    }
    
    @GetMapping("/entity-already-exists")
    fun entityAlreadyExists(): String {
        throw EntityAlreadyExistsException("User", "test@example.com")
    }
    
    @GetMapping("/conflict")
    fun conflict(): String {
        throw ConflictException("update user", "user is currently being modified")
    }
    
    @GetMapping("/invalid-data")
    fun invalidData(): String {
        throw InvalidDataException("email", "invalid-email", "not a valid email format")
    }
    
    @GetMapping("/service-operation")
    fun serviceOperation(): String {
        throw ServiceOperationException("EmailService", "send email", "SMTP server unavailable")
    }
    
    @GetMapping("/external-service")
    fun externalService(): String {
        throw ExternalServiceException("GoogleAPI", "authenticate user")
    }
    
    @GetMapping("/file-operation")
    fun fileOperation(): String {
        throw FileOperationException("upload", "profile.jpg", "file too large")
    }
    
    @GetMapping("/capacity-exceeded")
    fun capacityExceeded(): String {
        throw CapacityExceededException("event", 50, 50)
    }
    
    @GetMapping("/unavailable")
    fun unavailable(): String {
        throw UnavailableException("Kids Service", "age group not available")
    }
    
    @GetMapping("/bad-credentials")
    fun badCredentials(): String {
        throw BadCredentialsException("Invalid username or password")
    }
    
    @GetMapping("/user-not-found")
    fun userNotFound(): String {
        throw UsernameNotFoundException("User not found")
    }
    
    @GetMapping("/access-denied")
    fun accessDenied(): String {
        throw AccessDeniedException("Access denied")
    }
    
    @GetMapping("/illegal-argument")
    fun illegalArgument(): String {
        throw IllegalArgumentException("Invalid argument provided")
    }
    
    @GetMapping("/illegal-state")
    fun illegalState(): String {
        throw IllegalStateException("Invalid operation state")
    }
    
    @GetMapping("/runtime-exception")
    fun runtimeException(): String {
        throw RuntimeException("Something went wrong")
    }
    
    @GetMapping("/generic-exception")
    fun genericException(): String {
        throw Exception("Unexpected error")
    }
    
    @PostMapping("/validation")
    fun validation(@Valid @RequestBody request: TestValidationRequest): String {
        return "success"
    }
    
    data class TestValidationRequest(
        @field:NotBlank(message = "Name is required")
        val name: String,
        
        @field:Email(message = "Email should be valid")
        @field:NotBlank(message = "Email is required")
        val email: String
    )
}

@WebMvcTest(controllers = [TestController::class, GlobalExceptionHandler::class])
class GlobalExceptionHandlerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @TestConfiguration
    class TestConfig {
        @Bean
        fun globalExceptionHandler(): GlobalExceptionHandler {
            return GlobalExceptionHandler()
        }
    }
    
    @Test
    fun `should handle EntityNotFoundException with 404 status`() {
        mockMvc.perform(get("/test/entity-not-found"))
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User with identifier '123' not found"))
    }
    
    @Test
    fun `should handle UnauthorizedActionException with 403 status`() {
        mockMvc.perform(get("/test/unauthorized-action"))
            .andExpect(status().isForbidden)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Unauthorized to perform action: delete user - insufficient permissions"))
    }
    
    @Test
    fun `should handle BusinessRuleViolationException with 400 status`() {
        mockMvc.perform(get("/test/business-rule-violation"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Business rule violation: maximum capacity - event is full"))
    }
    
    @Test
    fun `should handle EntityAlreadyExistsException with 409 status`() {
        mockMvc.perform(get("/test/entity-already-exists"))
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User with identifier 'test@example.com' already exists"))
    }
    
    @Test
    fun `should handle ConflictException with 409 status`() {
        mockMvc.perform(get("/test/conflict"))
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Cannot perform operation 'update user': user is currently being modified"))
    }
    
    @Test
    fun `should handle InvalidDataException with 400 status`() {
        mockMvc.perform(get("/test/invalid-data"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid data for field 'email' with value 'invalid-email': not a valid email format"))
    }
    
    @Test
    fun `should handle ServiceOperationException with 500 status`() {
        mockMvc.perform(get("/test/service-operation"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("EmailService service failed to perform 'send email': SMTP server unavailable"))
    }
    
    @Test
    fun `should handle ExternalServiceException with 503 status`() {
        mockMvc.perform(get("/test/external-service"))
            .andExpect(status().isServiceUnavailable)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("External service temporarily unavailable"))
    }
    
    @Test
    fun `should handle FileOperationException with 400 status`() {
        mockMvc.perform(get("/test/file-operation"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("File operation 'upload' failed for file 'profile.jpg': file too large"))
    }
    
    @Test
    fun `should handle CapacityExceededException with 409 status`() {
        mockMvc.perform(get("/test/capacity-exceeded"))
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Capacity exceeded for 'event': 50/50"))
    }
    
    @Test
    fun `should handle UnavailableException with 409 status`() {
        mockMvc.perform(get("/test/unavailable"))
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Kids Service is not available: age group not available"))
    }
    
    @Test
    fun `should handle BadCredentialsException with 401 status`() {
        mockMvc.perform(get("/test/bad-credentials"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid credentials"))
    }
    
    @Test
    fun `should handle UsernameNotFoundException with 404 status`() {
        mockMvc.perform(get("/test/user-not-found"))
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User not found"))
    }
    
    @Test
    fun `should handle AccessDeniedException with 403 status`() {
        mockMvc.perform(get("/test/access-denied"))
            .andExpect(status().isForbidden)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Access denied"))
    }
    
    @Test
    fun `should handle IllegalArgumentException with 400 status`() {
        mockMvc.perform(get("/test/illegal-argument"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid argument provided"))
    }
    
    @Test
    fun `should handle IllegalStateException with 409 status`() {
        mockMvc.perform(get("/test/illegal-state"))
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid operation state"))
    }
    
    @Test
    fun `should handle RuntimeException with 500 status`() {
        mockMvc.perform(get("/test/runtime-exception"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("An error occurred while processing your request"))
    }
    
    @Test
    fun `should handle generic Exception with 500 status`() {
        mockMvc.perform(get("/test/generic-exception"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
    }
    
    @Test
    fun `should handle validation errors with 400 status and field details`() {
        val invalidRequest = """
            {
                "name": "",
                "email": "invalid-email"
            }
        """.trimIndent()
        
        mockMvc.perform(
            post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.data.name").value("Name is required"))
            .andExpect(jsonPath("$.data.email").value("Email should be valid"))
    }
    
    @Test
    fun `should handle malformed JSON with 400 status`() {
        val malformedJson = """
            {
                "name": "test"
                "email": "missing comma"
            }
        """.trimIndent()
        
        mockMvc.perform(
            post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid request format. Please check your JSON syntax."))
    }
}