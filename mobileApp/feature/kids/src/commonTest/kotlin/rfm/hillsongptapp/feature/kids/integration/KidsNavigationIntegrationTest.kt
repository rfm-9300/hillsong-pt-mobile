package rfm.hillsongptapp.feature.kids.integration

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import rfm.hillsongptapp.core.navigation.KidsNav
import rfm.hillsongptapp.core.navigation.HomeNav
import rfm.hillsongptapp.feature.kids.navigation.generateKidsBreadcrumbs
import rfm.hillsongptapp.feature.kids.navigation.KidsBreadcrumb

/**
 * Integration tests for Kids navigation with the app's navigation system
 */
class KidsNavigationIntegrationTest {
    
    @Test
    fun `kids navigation routes are properly defined`() {
        // Test that all kids navigation routes exist and are serializable
        val managementRoute = KidsNav.Management
        val registrationRoute = KidsNav.Registration
        val servicesRoute = KidsNav.Services
        val servicesForChildRoute = KidsNav.ServicesForChild("test_child_id")
        val checkInRoute = KidsNav.CheckIn("test_child_id")
        val checkOutRoute = KidsNav.CheckOut("test_child_id")
        val editChildRoute = KidsNav.EditChild("test_child_id")
        val reportsRoute = KidsNav.Reports
        
        // Verify routes are not null
        assertNotNull(managementRoute)
        assertNotNull(registrationRoute)
        assertNotNull(servicesRoute)
        assertNotNull(servicesForChildRoute)
        assertNotNull(checkInRoute)
        assertNotNull(checkOutRoute)
        assertNotNull(editChildRoute)
        assertNotNull(reportsRoute)
        
        // Verify parameterized routes contain correct child ID
        assertEquals("test_child_id", servicesForChildRoute.childId)
        assertEquals("test_child_id", checkInRoute.childId)
        assertEquals("test_child_id", checkOutRoute.childId)
        assertEquals("test_child_id", editChildRoute.childId)
    }
    
    @Test
    fun `kids screen is integrated with home navigation`() {
        // Test that KidsScreen route exists in HomeNav
        val kidsScreenRoute = HomeNav.KidsScreen
        assertNotNull(kidsScreenRoute)
    }
    
    @Test
    fun `breadcrumb navigation works correctly`() {
        // Test breadcrumb generation for different routes
        
        // Management screen - should have only one breadcrumb
        val managementBreadcrumbs = generateKidsBreadcrumbs(KidsNav.Management)
        assertEquals(1, managementBreadcrumbs.size)
        assertEquals("Kids Management", managementBreadcrumbs[0].title)
        assertEquals(false, managementBreadcrumbs[0].isClickable)
        
        // Registration screen - should have two breadcrumbs
        val registrationBreadcrumbs = generateKidsBreadcrumbs(KidsNav.Registration)
        assertEquals(2, registrationBreadcrumbs.size)
        assertEquals("Kids Management", registrationBreadcrumbs[0].title)
        assertEquals(true, registrationBreadcrumbs[0].isClickable)
        assertEquals("Register Child", registrationBreadcrumbs[1].title)
        assertEquals(false, registrationBreadcrumbs[1].isClickable)
        
        // Services screen - should have two breadcrumbs
        val servicesBreadcrumbs = generateKidsBreadcrumbs(KidsNav.Services)
        assertEquals(2, servicesBreadcrumbs.size)
        assertEquals("Kids Management", servicesBreadcrumbs[0].title)
        assertEquals("Services", servicesBreadcrumbs[1].title)
        
        // Services for child - should have three breadcrumbs
        val servicesForChildBreadcrumbs = generateKidsBreadcrumbs(KidsNav.ServicesForChild("child123"))
        assertEquals(3, servicesForChildBreadcrumbs.size)
        assertEquals("Kids Management", servicesForChildBreadcrumbs[0].title)
        assertEquals("Services", servicesForChildBreadcrumbs[1].title)
        assertEquals("Child Services", servicesForChildBreadcrumbs[2].title)
        
        // Check-in screen - should have two breadcrumbs
        val checkInBreadcrumbs = generateKidsBreadcrumbs(KidsNav.CheckIn("child123"))
        assertEquals(2, checkInBreadcrumbs.size)
        assertEquals("Kids Management", checkInBreadcrumbs[0].title)
        assertEquals("Check In", checkInBreadcrumbs[1].title)
        
        // Check-out screen - should have two breadcrumbs
        val checkOutBreadcrumbs = generateKidsBreadcrumbs(KidsNav.CheckOut("child123"))
        assertEquals(2, checkOutBreadcrumbs.size)
        assertEquals("Kids Management", checkOutBreadcrumbs[0].title)
        assertEquals("Check Out", checkOutBreadcrumbs[1].title)
        
        // Edit child screen - should have two breadcrumbs
        val editChildBreadcrumbs = generateKidsBreadcrumbs(KidsNav.EditChild("child123"))
        assertEquals(2, editChildBreadcrumbs.size)
        assertEquals("Kids Management", editChildBreadcrumbs[0].title)
        assertEquals("Edit Child", editChildBreadcrumbs[1].title)
        
        // Reports screen - should have two breadcrumbs
        val reportsBreadcrumbs = generateKidsBreadcrumbs(KidsNav.Reports)
        assertEquals(2, reportsBreadcrumbs.size)
        assertEquals("Kids Management", reportsBreadcrumbs[0].title)
        assertEquals("Reports", reportsBreadcrumbs[1].title)
    }
    
    @Test
    fun `deep linking routes are properly structured`() {
        // Test that parameterized routes can be constructed with different IDs
        val childIds = listOf("child1", "child2", "child_with_underscore", "child-with-dash")
        
        childIds.forEach { childId ->
            val servicesForChildRoute = KidsNav.ServicesForChild(childId)
            val checkInRoute = KidsNav.CheckIn(childId)
            val checkOutRoute = KidsNav.CheckOut(childId)
            val editChildRoute = KidsNav.EditChild(childId)
            
            assertEquals(childId, servicesForChildRoute.childId)
            assertEquals(childId, checkInRoute.childId)
            assertEquals(childId, checkOutRoute.childId)
            assertEquals(childId, editChildRoute.childId)
        }
    }
    
    @Test
    fun `navigation state management works correctly`() {
        // Test that navigation state can be properly managed
        val breadcrumb = KidsBreadcrumb(
            title = "Test Screen",
            route = KidsNav.Management,
            isClickable = true
        )
        
        assertEquals("Test Screen", breadcrumb.title)
        assertEquals(KidsNav.Management, breadcrumb.route)
        assertTrue(breadcrumb.isClickable)
    }
}