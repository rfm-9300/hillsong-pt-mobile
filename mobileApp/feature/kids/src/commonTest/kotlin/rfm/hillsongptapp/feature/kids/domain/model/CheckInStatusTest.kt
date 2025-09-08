package rfm.hillsongptapp.feature.kids.domain.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckInStatusTest {
    
    @Test
    fun testCheckInStatusSerialization() {
        val statuses = listOf(
            CheckInStatus.CHECKED_OUT,
            CheckInStatus.CHECKED_IN,
            CheckInStatus.NOT_IN_SERVICE
        )
        
        statuses.forEach { status ->
            val json = Json.encodeToString(CheckInStatus.serializer(), status)
            val deserializedStatus = Json.decodeFromString(CheckInStatus.serializer(), json)
            assertEquals(status, deserializedStatus)
        }
    }
    
    @Test
    fun testIsAvailableForCheckIn() {
        assertTrue(CheckInStatus.CHECKED_OUT.isAvailableForCheckIn())
        assertTrue(CheckInStatus.NOT_IN_SERVICE.isAvailableForCheckIn())
        assertFalse(CheckInStatus.CHECKED_IN.isAvailableForCheckIn())
    }
    
    @Test
    fun testCanBeCheckedOut() {
        assertTrue(CheckInStatus.CHECKED_IN.canBeCheckedOut())
        assertFalse(CheckInStatus.CHECKED_OUT.canBeCheckedOut())
        assertFalse(CheckInStatus.NOT_IN_SERVICE.canBeCheckedOut())
    }
    
    @Test
    fun testGetDisplayName() {
        assertEquals("Checked Out", CheckInStatus.CHECKED_OUT.getDisplayName())
        assertEquals("Checked In", CheckInStatus.CHECKED_IN.getDisplayName())
        assertEquals("Not in Service", CheckInStatus.NOT_IN_SERVICE.getDisplayName())
    }
}