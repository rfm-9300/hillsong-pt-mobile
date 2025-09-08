package rfm.hillsongptapp.feature.kids.domain.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmergencyContactTest {
    
    private val validContact = EmergencyContact(
        name = "John Doe",
        phoneNumber = "+1234567890",
        relationship = "Father"
    )
    
    @Test
    fun testEmergencyContactSerialization() {
        val json = Json.encodeToString(EmergencyContact.serializer(), validContact)
        val deserializedContact = Json.decodeFromString(EmergencyContact.serializer(), json)
        
        assertEquals(validContact, deserializedContact)
    }
    
    @Test
    fun testValidPhoneNumber() {
        val validNumbers = listOf(
            "+1234567890",
            "123-456-7890",
            "(123) 456-7890",
            "123 456 7890",
            "+1 (123) 456-7890"
        )
        
        validNumbers.forEach { phoneNumber ->
            val contact = validContact.copy(phoneNumber = phoneNumber)
            assertTrue(contact.isValidPhoneNumber(), "Phone number $phoneNumber should be valid")
        }
    }
    
    @Test
    fun testInvalidPhoneNumber() {
        val invalidNumbers = listOf(
            "123", // Too short
            "abc-def-ghij", // Contains letters
            "", // Empty
            "123-456", // Too short
            "123@456#7890" // Invalid characters
        )
        
        invalidNumbers.forEach { phoneNumber ->
            val contact = validContact.copy(phoneNumber = phoneNumber)
            assertFalse(contact.isValidPhoneNumber(), "Phone number $phoneNumber should be invalid")
        }
    }
    
    @Test
    fun testBlankNameValidation() {
        assertFailsWith<IllegalArgumentException> {
            EmergencyContact(
                name = "",
                phoneNumber = "+1234567890",
                relationship = "Father"
            )
        }
        
        assertFailsWith<IllegalArgumentException> {
            EmergencyContact(
                name = "   ",
                phoneNumber = "+1234567890",
                relationship = "Father"
            )
        }
    }
    
    @Test
    fun testBlankPhoneNumberValidation() {
        assertFailsWith<IllegalArgumentException> {
            EmergencyContact(
                name = "John Doe",
                phoneNumber = "",
                relationship = "Father"
            )
        }
        
        assertFailsWith<IllegalArgumentException> {
            EmergencyContact(
                name = "John Doe",
                phoneNumber = "   ",
                relationship = "Father"
            )
        }
    }
    
    @Test
    fun testBlankRelationshipValidation() {
        assertFailsWith<IllegalArgumentException> {
            EmergencyContact(
                name = "John Doe",
                phoneNumber = "+1234567890",
                relationship = ""
            )
        }
        
        assertFailsWith<IllegalArgumentException> {
            EmergencyContact(
                name = "John Doe",
                phoneNumber = "+1234567890",
                relationship = "   "
            )
        }
    }
}