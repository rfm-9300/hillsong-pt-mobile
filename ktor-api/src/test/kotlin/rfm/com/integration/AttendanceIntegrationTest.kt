package rfm.com.integration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
])
class AttendanceIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `attendance endpoints should be accessible`() {
        // Test that the attendance endpoints are properly configured and accessible
        mockMvc.perform(get("/api/attendance/currently-checked-in"))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `my-attendance endpoint should be accessible`() {
        mockMvc.perform(get("/api/attendance/my-attendance"))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "1", roles = ["ADMIN"])
    fun `admin endpoints should be accessible for admin users`() {
        mockMvc.perform(get("/api/attendance/frequent-attendees"))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `admin endpoints should be forbidden for regular users`() {
        mockMvc.perform(get("/api/attendance/frequent-attendees"))
            .andExpect(status().isForbidden)
    }
}