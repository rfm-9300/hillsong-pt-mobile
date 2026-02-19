package rfm.com.entity

/**
 * This file serves as an index for all MongoDB documents in the application.
 */

// Core documents
typealias UserDocument = User
typealias EventDocument = Event
typealias PostDocument = Post

// Supporting documents
typealias EncounterDocument = Encounter
typealias CalendarEventDocument = CalendarEvent
typealias YouTubeVideoDocument = YouTubeVideo

// Kids feature documents
typealias KidDocument = Kid
typealias KidAttendanceDocument = KidAttendance
typealias ServiceDocument = Service
typealias KidsServiceDocument = KidsService
typealias CheckInRequestDocument = CheckInRequest
typealias AttendanceDocument = Attendance

/**
 * Standard role names used in the system
 */
object RoleNames {
    const val USER = "USER"
    const val ADMIN = "ADMIN"
    const val STAFF = "STAFF"
}