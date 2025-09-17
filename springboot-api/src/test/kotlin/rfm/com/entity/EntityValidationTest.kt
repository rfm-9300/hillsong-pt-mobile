package rfm.com.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDateTime

class EntityValidationTest {

    @Test
    fun `should create User entity without errors`() {
        assertDoesNotThrow {
            User(
                email = "test@example.com",
                password = "hashedPassword",
                salt = "salt123",
                verified = true,
                authProvider = AuthProvider.LOCAL
            )
        }
    }

    @Test
    fun `should create UserProfile entity without errors`() {
        val user = User(
            email = "test@example.com",
            password = "hashedPassword",
            salt = "salt123"
        )
        
        assertDoesNotThrow {
            UserProfile(
                user = user,
                firstName = "John",
                lastName = "Doe",
                email = "test@example.com",
                phone = "+1234567890"
            )
        }
    }

    @Test
    fun `should create Event entity without errors`() {
        val user = User(
            email = "organizer@example.com",
            password = "hashedPassword",
            salt = "salt123"
        )
        
        val organizer = UserProfile(
            user = user,
            firstName = "Jane",
            lastName = "Smith",
            email = "organizer@example.com",
            phone = "+1234567890"
        )
        
        assertDoesNotThrow {
            Event(
                title = "Test Event",
                description = "This is a test event",
                date = LocalDateTime.now().plusDays(1),
                location = "Test Location",
                organizer = organizer,
                maxAttendees = 50
            )
        }
    }

    @Test
    fun `should create Post entity without errors`() {
        val user = User(
            email = "author@example.com",
            password = "hashedPassword",
            salt = "salt123"
        )
        
        val author = UserProfile(
            user = user,
            firstName = "Bob",
            lastName = "Johnson",
            email = "author@example.com",
            phone = "+1234567890"
        )
        
        assertDoesNotThrow {
            Post(
                title = "Test Post",
                content = "This is a test post content",
                author = author
            )
        }
    }

    @Test
    fun `should validate entity relationships`() {
        val user = User(
            email = "test@example.com",
            password = "hashedPassword",
            salt = "salt123"
        )
        
        val profile = UserProfile(
            user = user,
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "+1234567890"
        )
        
        val event = Event(
            title = "Test Event",
            description = "Test Description",
            date = LocalDateTime.now().plusDays(1),
            location = "Test Location",
            organizer = profile,
            maxAttendees = 10
        )
        
        val post = Post(
            title = "Test Post",
            content = "Test Content",
            author = profile
        )
        
        // Test event attendee management
        assertDoesNotThrow {
            event.addAttendee(user)
            assert(event.attendeeCount == 1)
            assert(event.availableSpots == 9)
        }
        
        // Test post like management
        assertDoesNotThrow {
            post.addLike(user)
            assert(post.likeCount == 1)
            assert(post.isLikedBy(user))
        }
    }
}