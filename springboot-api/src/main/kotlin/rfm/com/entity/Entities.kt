package rfm.com.entity

/**
 * This file serves as an index for all JPA entities in the application.
 * It provides a central location to reference all entity classes for
 * configuration and documentation purposes.
 */

// Core entities
typealias UserEntity = User
typealias UserProfileEntity = UserProfile
typealias EventEntity = Event
typealias PostEntity = Post

// Supporting entities
typealias PostCommentEntity = PostComment
typealias UserTokenEntity = UserToken
typealias PasswordResetEntity = PasswordReset
typealias RoleEntity = Role
typealias UserRoleEntity = UserRole

// Specialized feature entities
typealias AttendanceEntity = Attendance
typealias KidEntity = Kid
typealias KidAttendanceEntity = KidAttendance
typealias ServiceEntity = Service
typealias KidsServiceEntity = KidsService
typealias CheckInRequestEntity = CheckInRequest

/**
 * List of all entity classes for JPA configuration
 */
val ALL_ENTITIES = listOf(
    User::class.java,
    UserProfile::class.java,
    Event::class.java,
    Post::class.java,
    PostComment::class.java,
    UserToken::class.java,
    PasswordReset::class.java,
    Attendance::class.java,
    Kid::class.java,
    KidAttendance::class.java,
    Service::class.java,
    KidsService::class.java,
    CheckInRequest::class.java,
    Role::class.java,
    UserRole::class.java
)