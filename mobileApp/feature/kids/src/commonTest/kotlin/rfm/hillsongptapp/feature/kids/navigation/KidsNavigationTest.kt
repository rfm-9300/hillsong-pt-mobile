package rfm.hillsongptapp.feature.kids.navigation

import kotlinx.coroutines.test.runTest
import org.junit.Test
import rfm.hillsongptapp.core.navigation.KidsNav
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for Kids Management navigation functionality
 */
class KidsNavigationTest {
    
    @Test
    fun `navigation routes are properly defined`() = runTest {
        // Test that all navigation routes are properly serializable
        val routes = listOf(
            KidsNav.Management,
            KidsNav.Registration,
            KidsNav.Services,
            KidsNav.ServicesForChild("test-child"),
            KidsNav.CheckIn("test-child"),
            KidsNav.CheckOut("test-child"),
            KidsNav.EditChild("test-child"),
            KidsNav.Reports
        )
        
        // Verify all routes are created successfully
        assertEquals(8, routes.size)
        assertTrue(routes.contains(KidsNav.Management))
        assertTrue(routes.any { it is KidsNav.ServicesForChild })
    }
    
    @Test
    fun `navigation routes have correct types`() = runTest {
        // Test route types
        assertTrue(KidsNav.Management is KidsNav)
        assertTrue(KidsNav.Registration is KidsNav)
        assertTrue(KidsNav.Services is KidsNav)
        assertTrue(KidsNav.ServicesForChild("test") is KidsNav.ServicesForChild)
        assertTrue(KidsNav.CheckIn("test") is KidsNav.CheckIn)
        assertTrue(KidsNav.CheckOut("test") is KidsNav.CheckOut)
        assertTrue(KidsNav.EditChild("test") is KidsNav.EditChild)
        assertTrue(KidsNav.Reports is KidsNav)
    }
    
    @Test
    fun `child-specific routes contain child id`() = runTest {
        val childId = "test-child-123"
        
        val servicesRoute = KidsNav.ServicesForChild(childId)
        val checkInRoute = KidsNav.CheckIn(childId)
        val checkOutRoute = KidsNav.CheckOut(childId)
        val editRoute = KidsNav.EditChild(childId)
        
        assertEquals(childId, servicesRoute.childId)
        assertEquals(childId, checkInRoute.childId)
        assertEquals(childId, checkOutRoute.childId)
        assertEquals(childId, editRoute.childId)
    }
    
    @Test
    fun `navigation flows are properly defined`() = runTest {
        // Test navigation flow types
        val registrationFlow = KidsNavigationFlow.Registration
        val checkInFlow = KidsNavigationFlow.CheckInFlow("child-123")
        val checkOutFlow = KidsNavigationFlow.CheckOutFlow("child-123")
        val editFlow = KidsNavigationFlow.EditChildFlow("child-123")
        val reportsFlow = KidsNavigationFlow.ReportsFlow
        
        assertTrue(registrationFlow is KidsNavigationFlow.Registration)
        assertTrue(checkInFlow is KidsNavigationFlow.CheckInFlow)
        assertTrue(checkOutFlow is KidsNavigationFlow.CheckOutFlow)
        assertTrue(editFlow is KidsNavigationFlow.EditChildFlow)
        assertTrue(reportsFlow is KidsNavigationFlow.ReportsFlow)
        
        assertEquals("child-123", checkInFlow.childId)
        assertEquals("child-123", checkOutFlow.childId)
        assertEquals("child-123", editFlow.childId)
    }
}