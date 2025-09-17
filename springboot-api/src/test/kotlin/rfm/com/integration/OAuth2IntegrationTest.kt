package rfm.com.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rfm.com.dto.FacebookAuthRequest
import rfm.com.dto.GoogleAuthRequest

/**
 * Integration test for OAuth2 functionality.
 * Validates that OAuth2 endpoints are properly configured and handle requests
 * in the same format as the original Ktor implementation.
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:oauth2test",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.oauth2.google.client-id=test-google-client-id",
    "app.oauth2.facebook.app-id=test-facebook-app-id",
    "app.oauth2.facebook.app-secret=test-facebook-app-secret",
    "logging.level.rfm.com=INFO"
])
class OAuth2IntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should handle Google OAuth2 login request with proper endpoint structure`() {
        val googleAuthRequest = GoogleAuthRequest(
            idToken = "mock.google.token"
        )

        mockMvc.perform(
            post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(googleAuthRequest))
        )
        .andExpect(status().isBadRequest) // Expected due to mock token
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `should handle Facebook OAuth2 login request with proper endpoint structure`() {
        val facebookAuthRequest = FacebookAuthRequest(
            accessToken = "mock-facebook-access-token"
        )

        mockMvc.perform(
            post("/api/auth/facebook-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(facebookAuthRequest))
        )
        .andExpect(status().isBadRequest) // Expected due to mock token
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `should validate Google OAuth2 request format`() {
        val invalidGoogleRequest = mapOf("invalidField" to "value")

        mockMvc.perform(
            post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidGoogleRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
    }

    @Test
    fun `should validate Facebook OAuth2 request format`() {
        val invalidFacebookRequest = mapOf("invalidField" to "value")

        mockMvc.perform(
            post("/api/auth/facebook-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidFacebookRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
    }

    @Test
    fun `should handle empty Google token gracefully`() {
        val googleAuthRequest = GoogleAuthRequest(
            idToken = ""
        )

        mockMvc.perform(
            post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(googleAuthRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
    }

    @Test
    fun `should handle empty Facebook token gracefully`() {
        val facebookAuthRequest = FacebookAuthRequest(
            accessToken = ""
        )

        mockMvc.perform(
            post("/api/auth/facebook-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(facebookAuthRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
    }

    @Test
    fun `should maintain consistent API response format for OAuth2 endpoints`() {
        val googleAuthRequest = GoogleAuthRequest(
            idToken = "mock.google.token"
        )

        mockMvc.perform(
            post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(googleAuthRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").exists())
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.data").doesNotExist()) // Should not have data on error
    }

    @Test
    fun `should handle malformed JSON in OAuth2 requests`() {
        val malformedJson = "{ invalid json }"

        mockMvc.perform(
            post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson)
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `should handle missing Content-Type header in OAuth2 requests`() {
        val googleAuthRequest = GoogleAuthRequest(
            idToken = "mock.google.token"
        )

        mockMvc.perform(
            post("/api/auth/google-login")
                .content(objectMapper.writeValueAsString(googleAuthRequest))
        )
        .andExpect(status().isUnsupportedMediaType)
    }
}