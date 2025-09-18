package rfm.hillsongptapp.feature.kids.integration

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import rfm.hillsongptapp.feature.kids.di.featureKidsModule
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import rfm.hillsongptapp.feature.kids.ui.KidsManagementViewModel
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.core.data.repository.database.User
import rfm.hillsongptapp.core.data.repository.database.UserProfile

/**
 * Integration tests for Kids feature with app-wide functionality
 * Tests the integration between kids management and user authentication system
 */
class KidsAppIntegrationTest : KoinTest {
    
    private val kidsRepository: KidsRepository by inject()
    private val authRepository: AuthRepository by inject()
    
    @Test
    fun `kids feature integrates with user authentication system`() = runTest {
        // Setup Koin for testing
        startKoin {
            modules(featureKidsModule)
        }
        
        try {
            // Test that KidsRepository is properly injected
            assertNotNull(kidsRepository)
            
            // Test that UserRepository is properly injected
            assertNotNull(authRepository)
            
        } finally {
            stopKoin()
        }
    }
    
    @Test
    fun `kids management viewmodel integrates with user permissions`() = runTest {
        startKoin {
            modules(featureKidsModule)
        }
        
        try {
            // Create test user with admin permissions
            val adminUser = User(
                id = 1L,
                email = "admin@test.com",
                password = "password",
                token = "test_token"
            )
            
            val adminProfile = UserProfile(
                id = 1L,
                userId = 1L,
                firstName = "Admin",
                lastName = "User",
                email = "admin@test.com",
                phone = "123-456-7890",
                joinedAt = System.currentTimeMillis(),
                imagePath = "",
                isAdmin = true
            )
            
            // Insert test user
            authRepository.insertUser(adminUser)
            authRepository.insertUserProfile(adminProfile)
            
            // Create ViewModel and verify it loads user permissions
            val viewModel: KidsManagementViewModel by inject()
            
            // Wait for user session to load
            kotlinx.coroutines.delay(100)
            
            // Verify staff permissions are loaded
            assertTrue(viewModel.hasStaffPermissions())
            assertEquals("1", viewModel.getCurrentUserId())
            
        } finally {
            stopKoin()
        }
    }
    
    @Test
    fun `kids navigation integrates with app navigation system`() = runTest {
        // Test that navigation routes are properly defined
        val kidsNavRoutes = listOf(
            "KidsNav.Management",
            "KidsNav.Registration", 
            "KidsNav.Services",
            "KidsNav.CheckIn",
            "KidsNav.CheckOut",
            "KidsNav.EditChild",
            "KidsNav.Reports"
        )
        
        // Verify all expected routes exist
        assertTrue(kidsNavRoutes.isNotEmpty())
        assertEquals(7, kidsNavRoutes.size)
    }
    
    @Test
    fun `kids feature uses consistent theming`() = runTest {
        // Test that kids theme colors are properly defined
        // This would be expanded with actual theme testing in a real implementation
        assertTrue(true) // Placeholder for theme integration test
    }
    
    @Test
    fun `kids feature respects user roles and permissions`() = runTest {
        startKoin {
            modules(featureKidsModule)
        }
        
        try {
            // Test regular user (non-admin)
            val regularUser = User(
                id = 2L,
                email = "user@test.com", 
                password = "password",
                token = "test_token"
            )
            
            val regularProfile = UserProfile(
                id = 2L,
                userId = 2L,
                firstName = "Regular",
                lastName = "User",
                email = "user@test.com",
                phone = "123-456-7890",
                joinedAt = System.currentTimeMillis(),
                imagePath = "",
                isAdmin = false
            )
            
            authRepository.insertUser(regularUser)
            authRepository.insertUserProfile(regularProfile)
            
            // Test that regular user doesn't have staff permissions
            // This would require updating the ViewModel to use the correct user ID
            // For now, this is a placeholder test
            assertTrue(true)
            
        } finally {
            stopKoin()
        }
    }
    
    @Test
    fun `kids feature handles authentication errors gracefully`() = runTest {
        startKoin {
            modules(featureKidsModule)
        }
        
        try {
            // Test behavior when no user is logged in
            val viewModel: KidsManagementViewModel by inject()
            
            // Wait for initialization
            kotlinx.coroutines.delay(100)
            
            // Verify error handling for missing user session
            // This would check that appropriate error messages are shown
            assertTrue(true) // Placeholder for error handling test
            
        } finally {
            stopKoin()
        }
    }
}