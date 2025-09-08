package rfm.hillsongptapp.feature.kids.navigation

import kotlinx.coroutines.test.runTest
import org.junit.Test
import rfm.hillsongptapp.core.navigation.KidsNav
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for breadcrumb navigation functionality
 */
class BreadcrumbNavigationTest {
    
    @Test
    fun `generates correct breadcrumbs for management screen`() = runTest {
        val breadcrumbs = generateKidsBreadcrumbs(KidsNav.Management)
        
        assertEquals(1, breadcrumbs.size)
        assertEquals("Kids Management", breadcrumbs[0].title)
        assertEquals(KidsNav.Management, breadcrumbs[0].route)
        assertFalse(breadcrumbs[0].isClickable)
    }
    
    @Test
    fun `generates correct breadcrumbs for registration screen`() = runTest {
        val breadcrumbs = generateKidsBreadcrumbs(KidsNav.Registration)
        
        assertEquals(2, breadcrumbs.size)
        
        // First breadcrumb (Kids Management)
        assertEquals("Kids Management", breadcrumbs[0].title)
        assertEquals(KidsNav.Management, breadcrumbs[0].route)
        assertTrue(breadcrumbs[0].isClickable)
        
        // Second breadcrumb (Register Child)
        assertEquals("Register Child", breadcrumbs[1].title)
        assertEquals(KidsNav.Registration, breadcrumbs[1].route)
        assertFalse(breadcrumbs[1].isClickable)
    }
    
    @Test
    fun `generates correct breadcrumbs for services screen`() = runTest {
        val breadcrumbs = generateKidsBreadcrumbs(KidsNav.Services)
        
        assertEquals(2, breadcrumbs.size)
        
        // First breadcrumb (Kids Management)
        assertEquals("Kids Management", breadcrumbs[0].title)
        assertEquals(KidsNav.Management, breadcrumbs[0].route)
        assertTrue(breadcrumbs[0].isClickable)
        
        // Second breadcrumb (Services)
        assertEquals("Services", breadcrumbs[1].title)
        assertEquals(KidsNav.Services, breadcrumbs[1].route)
        assertFalse(breadcrumbs[1].isClickable)
    }
    
    @Test
    fun `generates correct breadcrumbs for services for child screen`() = runTest {
        val route = KidsNav.ServicesForChild("test-child-123")
        val breadcrumbs = generateKidsBreadcrumbs(route)
        
        assertEquals(3, breadcrumbs.size)
        
        // First breadcrumb (Kids Management)
        assertEquals("Kids Management", breadcrumbs[0].title)
        assertEquals(KidsNav.Management, breadcrumbs[0].route)
        assertTrue(breadcrumbs[0].isClickable)
        
        // Second breadcrumb (Services)
        assertEquals("Services", breadcrumbs[1].title)
        assertEquals(KidsNav.Services, breadcrumbs[1].route)
        assertTrue(breadcrumbs[1].isClickable)
        
        // Third breadcrumb (Child Services)
        assertEquals("Child Services", breadcrumbs[2].title)
        assertEquals(route, breadcrumbs[2].route)
        assertFalse(breadcrumbs[2].isClickable)
    }
    
    @Test
    fun `generates correct breadcrumbs for check-in screen`() = runTest {
        val route = KidsNav.CheckIn("test-child-123")
        val breadcrumbs = generateKidsBreadcrumbs(route)
        
        assertEquals(2, breadcrumbs.size)
        
        // First breadcrumb (Kids Management)
        assertEquals("Kids Management", breadcrumbs[0].title)
        assertEquals(KidsNav.Management, breadcrumbs[0].route)
        assertTrue(breadcrumbs[0].isClickable)
        
        // Second breadcrumb (Check In)
        assertEquals("Check In", breadcrumbs[1].title)
        assertEquals(route, breadcrumbs[1].route)
        assertFalse(breadcrumbs[1].isClickable)
    }
    
    @Test
    fun `generates correct breadcrumbs for check-out screen`() = runTest {
        val route = KidsNav.CheckOut("test-child-123")
        val breadcrumbs = generateKidsBreadcrumbs(route)
        
        assertEquals(2, breadcrumbs.size)
        
        // First breadcrumb (Kids Management)
        assertEquals("Kids Management", breadcrumbs[0].title)
        assertEquals(KidsNav.Management, breadcrumbs[0].route)
        assertTrue(breadcrumbs[0].isClickable)
        
        // Second breadcrumb (Check Out)
        assertEquals("Check Out", breadcrumbs[1].title)
        assertEquals(route, breadcrumbs[1].route)
        assertFalse(breadcrumbs[1].isClickable)
    }
    
    @Test
    fun `generates correct breadcrumbs for edit child screen`() = runTest {
        val route = KidsNav.EditChild("test-child-123")
        val breadcrumbs = generateKidsBreadcrumbs(route)
        
        assertEquals(2, breadcrumbs.size)
        
        // First breadcrumb (Kids Management)
        assertEquals("Kids Management", breadcrumbs[0].title)
        assertEquals(KidsNav.Management, breadcrumbs[0].route)
        assertTrue(breadcrumbs[0].isClickable)
        
        // Second breadcrumb (Edit Child)
        assertEquals("Edit Child", breadcrumbs[1].title)
        assertEquals(route, breadcrumbs[1].route)
        assertFalse(breadcrumbs[1].isClickable)
    }
    
    @Test
    fun `generates correct breadcrumbs for reports screen`() = runTest {
        val breadcrumbs = generateKidsBreadcrumbs(KidsNav.Reports)
        
        assertEquals(2, breadcrumbs.size)
        
        // First breadcrumb (Kids Management)
        assertEquals("Kids Management", breadcrumbs[0].title)
        assertEquals(KidsNav.Management, breadcrumbs[0].route)
        assertTrue(breadcrumbs[0].isClickable)
        
        // Second breadcrumb (Reports)
        assertEquals("Reports", breadcrumbs[1].title)
        assertEquals(KidsNav.Reports, breadcrumbs[1].route)
        assertFalse(breadcrumbs[1].isClickable)
    }
    
    @Test
    fun `last breadcrumb is always non-clickable`() = runTest {
        val routes = listOf(
            KidsNav.Management,
            KidsNav.Registration,
            KidsNav.Services,
            KidsNav.ServicesForChild("test"),
            KidsNav.CheckIn("test"),
            KidsNav.CheckOut("test"),
            KidsNav.EditChild("test"),
            KidsNav.Reports
        )
        
        routes.forEach { route ->
            val breadcrumbs = generateKidsBreadcrumbs(route)
            assertFalse(breadcrumbs.last().isClickable, "Last breadcrumb should not be clickable for route: $route")
        }
    }
    
    @Test
    fun `first breadcrumb is clickable except for management screen`() = runTest {
        val routes = listOf(
            KidsNav.Registration,
            KidsNav.Services,
            KidsNav.ServicesForChild("test"),
            KidsNav.CheckIn("test"),
            KidsNav.CheckOut("test"),
            KidsNav.EditChild("test"),
            KidsNav.Reports
        )
        
        routes.forEach { route ->
            val breadcrumbs = generateKidsBreadcrumbs(route)
            assertTrue(breadcrumbs.first().isClickable, "First breadcrumb should be clickable for route: $route")
            assertEquals("Kids Management", breadcrumbs.first().title)
        }
    }
}