package rfm.hillsongptapp.feature.kids.navigation

import kotlinx.coroutines.test.runTest
import org.junit.Test
import rfm.hillsongptapp.core.navigation.KidsNav
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Kids Management navigation flows
 */
class KidsNavigationFlowTest {
    
    @Test
    fun `navigation flows have correct child IDs`() = runTest {
        val childId = "test-child-123"
        
        val checkInFlow = KidsNavigationFlow.CheckInFlow(childId)
        val checkOutFlow = KidsNavigationFlow.CheckOutFlow(childId)
        val editFlow = KidsNavigationFlow.EditChildFlow(childId)
        
        assertEquals(childId, checkInFlow.childId)
        assertEquals(childId, checkOutFlow.childId)
        assertEquals(childId, editFlow.childId)
    }
    
    @Test
    fun `navigation flows are properly typed`() = runTest {
        val flows = listOf(
            KidsNavigationFlow.Registration,
            KidsNavigationFlow.CheckInFlow("child-123"),
            KidsNavigationFlow.CheckOutFlow("child-123"),
            KidsNavigationFlow.EditChildFlow("child-123"),
            KidsNavigationFlow.ReportsFlow
        )
        
        assertEquals(5, flows.size)
        assertTrue(flows[0] is KidsNavigationFlow.Registration)
        assertTrue(flows[1] is KidsNavigationFlow.CheckInFlow)
        assertTrue(flows[2] is KidsNavigationFlow.CheckOutFlow)
        assertTrue(flows[3] is KidsNavigationFlow.EditChildFlow)
        assertTrue(flows[4] is KidsNavigationFlow.ReportsFlow)
    }
}