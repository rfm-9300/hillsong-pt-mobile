package rfm.com.service

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import rfm.com.dto.UpdateProfileRequest
import rfm.com.dto.UserProfileResponse
import org.junit.jupiter.api.Assertions.assertNotNull

@SpringBootTest
@Transactional
class UserServiceProfileTest {

    @Test
    fun `UserService profile methods should be available`() {
        // This test just verifies that the UserService has the new profile management methods
        // and that they compile correctly
        
        val userServiceClass = UserService::class.java
        
        // Check that the new methods exist
        assertNotNull(userServiceClass.getMethod("updateUserProfile", Long::class.java, String::class.java, String::class.java, String::class.java))
        assertNotNull(userServiceClass.getMethod("updateUserProfileImage", Long::class.java, String::class.java))
        assertNotNull(userServiceClass.getMethod("getUserProfile", Long::class.java))
        assertNotNull(userServiceClass.getMethod("getAllUserProfiles"))
        assertNotNull(userServiceClass.getMethod("searchUserProfiles", String::class.java))
        assertNotNull(userServiceClass.getMethod("updateUserAdminStatus", Long::class.java, Boolean::class.java))
        assertNotNull(userServiceClass.getMethod("getAdminProfiles"))
        assertNotNull(userServiceClass.getMethod("deleteUser", Long::class.java))
    }
    
    @Test
    fun `ProfileController should be available`() {
        // This test verifies that the ProfileController class exists and compiles
        val profileControllerClass = Class.forName("rfm.com.controller.ProfileController")
        assertNotNull(profileControllerClass)
    }
    
    @Test
    fun `DTO classes should be available`() {
        // This test verifies that the new DTO classes exist and compile
        assertNotNull(UserProfileResponse::class.java)
        assertNotNull(UpdateProfileRequest::class.java)
    }
}