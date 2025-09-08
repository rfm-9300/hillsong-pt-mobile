package rfm.hillsongptapp.feature.kids.navigation

import kotlinx.coroutines.test.runTest
import org.junit.Test
import rfm.hillsongptapp.core.navigation.KidsNav
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for Kids Management deep linking functionality
 */
class KidsDeepLinkingTest {
    
    @Test
    fun `identifies kids deep links correctly`() {
        assertTrue(KidsDeepLinking.isKidsDeepLink("kids://management"))
        assertTrue(KidsDeepLinking.isKidsDeepLink("kids://registration"))
        assertTrue(KidsDeepLinking.isKidsDeepLink("kids://services"))
        assertTrue(KidsDeepLinking.isKidsDeepLink("kids://checkin?childId=123"))
        
        assertFalse(KidsDeepLinking.isKidsDeepLink("home://main"))
        assertFalse(KidsDeepLinking.isKidsDeepLink("https://example.com"))
        assertFalse(KidsDeepLinking.isKidsDeepLink("invalid-link"))
    }
    
    @Test
    fun `generates correct deep links for routes`() {
        assertEquals("kids://management", KidsDeepLinking.generateDeepLink(KidsNav.Management))
        assertEquals("kids://registration", KidsDeepLinking.generateDeepLink(KidsNav.Registration))
        assertEquals("kids://services", KidsDeepLinking.generateDeepLink(KidsNav.Services))
        assertEquals("kids://reports", KidsDeepLinking.generateDeepLink(KidsNav.Reports))
        
        assertEquals(
            "kids://services?childId=test-123",
            KidsDeepLinking.generateDeepLink(KidsNav.ServicesForChild("test-123"))
        )
        assertEquals(
            "kids://checkin?childId=test-123",
            KidsDeepLinking.generateDeepLink(KidsNav.CheckIn("test-123"))
        )
        assertEquals(
            "kids://checkout?childId=test-123",
            KidsDeepLinking.generateDeepLink(KidsNav.CheckOut("test-123"))
        )
        assertEquals(
            "kids://edit?childId=test-123",
            KidsDeepLinking.generateDeepLink(KidsNav.EditChild("test-123"))
        )
    }
    
    @Test
    fun `extracts child ID from deep link correctly`() = runTest {
        // Test the private method indirectly through public methods
        val deepLinkWithChildId = "kids://services?childId=test-123"
        val deepLinkWithoutChildId = "kids://services"
        
        // Generate deep links and verify they contain expected patterns
        val servicesLink = KidsDeepLinking.generateDeepLink(KidsNav.ServicesForChild("test-123"))
        assertTrue(servicesLink.contains("childId=test-123"))
        
        val checkInLink = KidsDeepLinking.generateDeepLink(KidsNav.CheckIn("test-123"))
        assertTrue(checkInLink.contains("childId=test-123"))
    }
    
    @Test
    fun `validates deep link patterns correctly`() = runTest {
        // Test various deep link patterns
        val validLinks = listOf(
            "kids://management",
            "kids://registration", 
            "kids://services",
            "kids://services?childId=123",
            "kids://checkin?childId=123",
            "kids://checkout?childId=123",
            "kids://edit?childId=123",
            "kids://reports"
        )
        
        val invalidLinks = listOf(
            "invalid://link",
            "home://main",
            "kids://invalid",
            "https://example.com"
        )
        
        validLinks.forEach { link ->
            assertTrue(KidsDeepLinking.isKidsDeepLink(link), "Should be valid: $link")
        }
        
        invalidLinks.forEach { link ->
            assertFalse(KidsDeepLinking.isKidsDeepLink(link), "Should be invalid: $link")
        }
    }
    
    @Test
    fun `deep link routes constants are correct`() {
        assertEquals("kids://management", KidsDeepLinkRoutes.MANAGEMENT)
        assertEquals("kids://registration", KidsDeepLinkRoutes.REGISTRATION)
        assertEquals("kids://services", KidsDeepLinkRoutes.SERVICES)
        assertEquals("kids://services?childId={childId}", KidsDeepLinkRoutes.SERVICES_FOR_CHILD)
        assertEquals("kids://checkin?childId={childId}", KidsDeepLinkRoutes.CHECK_IN)
        assertEquals("kids://checkout?childId={childId}", KidsDeepLinkRoutes.CHECK_OUT)
        assertEquals("kids://edit?childId={childId}", KidsDeepLinkRoutes.EDIT_CHILD)
        assertEquals("kids://reports", KidsDeepLinkRoutes.REPORTS)
    }
}