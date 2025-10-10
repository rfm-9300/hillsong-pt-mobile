package rfm.com.controller

/**
 * API Endpoints Index - Provides IDE navigation to all controller endpoints
 * 
 * This object serves as a centralized index of all API endpoints in the application.
 * Each property references the actual controller method, allowing for easy IDE navigation
 * and refactoring support.
 * 
 * Usage: Click on any property to navigate to the corresponding controller method.
 */
object ApiEndpointsIndex {

    /**
     * Authentication Endpoints
     * Base Path: /api/auth
     */
    object Auth {
        /** POST /api/auth/login - User login with email and password */
        val login = AuthController::login
        
        /** POST /api/auth/signup - User registration */
        val signup = AuthController::signup
        
        /** POST /api/auth/verify - Email verification with token */
        val verify = AuthController::verify
        
        /** GET /api/auth/verify - Email verification via GET (for email links) */
        val verifyByToken = AuthController::verifyByToken
        
        /** POST /api/auth/forgot-password - Request password reset */
        val forgotPassword = AuthController::forgotPassword
        
        /** POST /api/auth/reset-password - Reset password with token */
        val resetPassword = AuthController::resetPassword
        
        /** POST /api/auth/google-login - Google OAuth2 authentication */
        val googleLogin = AuthController::googleLogin
        
        /** POST /api/auth/facebook-login - Facebook OAuth2 authentication */
        val facebookLogin = AuthController::facebookLogin
        
        /** POST /api/auth/refresh - Refresh JWT token (placeholder) */
        val refreshToken = AuthController::refreshToken
        
        /** POST /api/auth/logout - User logout (placeholder) */
        val logout = AuthController::logout
    }

    /**
     * Attendance Endpoints
     * Base Path: /api/attendance
     */
    object Attendance {
        /** POST /api/attendance/check-in - Check in to an event, service, or kids service */
        val checkIn = AttendanceController::checkIn
        
        /** POST /api/attendance/check-out - Check out from an attendance record */
        val checkOut = AttendanceController::checkOut
        
        /** GET /api/attendance/my-attendance - Get attendance records for current user */
        val getMyAttendance = AttendanceController::getMyAttendance
        
        /** GET /api/attendance/user/{userId} - Get attendance records for specific user (ADMIN) */
        val getUserAttendance = AttendanceController::getUserAttendance
        
        /** GET /api/attendance/event/{eventId} - Get attendance records for specific event */
        val getEventAttendance = AttendanceController::getEventAttendance
        
        /** GET /api/attendance/service/{serviceId} - Get attendance records for specific service */
        val getServiceAttendance = AttendanceController::getServiceAttendance
        
        /** GET /api/attendance/kids-service/{kidsServiceId} - Get attendance records for specific kids service */
        val getKidsServiceAttendance = AttendanceController::getKidsServiceAttendance
        
        /** GET /api/attendance/currently-checked-in - Get currently checked-in users */
        val getCurrentlyCheckedIn = AttendanceController::getCurrentlyCheckedIn
        
        /** POST /api/attendance/stats - Get attendance statistics and analytics */
        val getAttendanceStats = AttendanceController::getAttendanceStats
        
        /** GET /api/attendance/frequent-attendees - Get most frequent attendees (ADMIN) */
        val getMostFrequentAttendees = AttendanceController::getMostFrequentAttendees
        
        /** POST /api/attendance/bulk-check-in - Bulk check-in multiple users (ADMIN) */
        val bulkCheckIn = AttendanceController::bulkCheckIn
        
        /** PUT /api/attendance/{attendanceId}/status - Update attendance status (ADMIN) */
        val updateAttendanceStatus = AttendanceController::updateAttendanceStatus
        
        /** GET /api/attendance/by-type/{type} - Get attendance records by type */
        val getAttendanceByType = AttendanceController::getAttendanceByType
        
        /** GET /api/attendance/by-status/{status} - Get attendance records by status */
        val getAttendanceByStatus = AttendanceController::getAttendanceByStatus
    }

    /**
     * Event Endpoints
     * Base Path: /api/events
     */
    object Events {
        /** GET /api/events - Get all events with pagination and sorting */
        val getAllEvents = EventController::getAllEvents
        
        /** GET /api/events/upcoming - Get upcoming events */
        val getUpcomingEvents = EventController::getUpcomingEvents
        
        /** GET /api/events/{id} - Get event by ID */
        val getEventById = EventController::getEventById
        
        /** POST /api/events - Create a new event */
        val createEvent = EventController::createEvent
        
        /** PUT /api/events/{id} - Update an existing event */
        val updateEvent = EventController::updateEvent
        
        /** DELETE /api/events/{id} - Delete an event */
        val deleteEvent = EventController::deleteEvent
        
        /** POST /api/events/{id}/join - Join an event */
        val joinEvent = EventController::joinEvent
        
        /** POST /api/events/{id}/leave - Leave an event */
        val leaveEvent = EventController::leaveEvent
        
        /** POST /api/events/{eventId}/approve/{userId} - Approve user for event (organizer only) */
        val approveUserForEvent = EventController::approveUserForEvent
        
        /** GET /api/events/{id}/status - Get user's status for specific event */
        val getUserEventStatus = EventController::getUserEventStatus
        
        /** GET /api/events/my-events - Get events organized by current user */
        val getMyEvents = EventController::getMyEvents
        
        /** GET /api/events/attending - Get events current user is attending */
        val getEventsAttending = EventController::getEventsAttending
        
        /** GET /api/events/waiting-list - Get events current user is on waiting list for */
        val getEventsOnWaitingList = EventController::getEventsOnWaitingList
        
        /** GET /api/events/search - Search events by title or location */
        val searchEvents = EventController::searchEvents
    }

    /**
     * File Endpoints
     * Base Path: /api/files
     */
    object Files {
        /** GET /api/files/{subDirectory}/{fileName} - Serve uploaded files from subdirectory */
        val serveFile = FileController::serveFile
        
        /** GET /api/files/{fileName} - Serve files from root upload directory */
        val serveRootFile = FileController::serveRootFile
        
        /** GET /api/files/{subDirectory}/{fileName}/info - Get file information */
        val getFileInfo = FileController::getFileInfo
    }

    /**
     * Health Endpoints
     * Base Path: /api
     */
    object Health {
        /** GET /api/health - Application health check */
        val health = HealthController::health
    }

    /**
     * Kids Management Endpoints
     * Base Path: /api/kids
     */
    object Kids {
        /** GET /api/kids/services - Get all available kids services (now includes serviceDate) */
        val getServices = KidsController::getServices
        
        /** GET /api/kids/services/{serviceId} - Get specific kids service (now includes serviceDate) */
        val getService = KidsController::getService

        /** POST /api/kids/children - Register a new child */
        val registerChild = KidsController::registerChild
        
        /** GET /api/kids/children - Get all children for current user */
        val getChildren = KidsController::getChildren
        
        /** GET /api/kids/children/parent/{parentId} - Get children for specific parent */
        val getChildrenByParent = KidsController::getChildrenByParent
        
        /** GET /api/kids/children/{childId} - Get specific child */
        val getChild = KidsController::getChild
        
        /** PUT /api/kids/children/{childId} - Update a child */
        val updateChild = KidsController::updateChild
        
        /** DELETE /api/kids/children/{childId} - Delete a child */
        val deleteChild = KidsController::deleteChild
        
        /** POST /api/kids/checkin - Check in a child to a service */
        val checkInChild = KidsController::checkInChild
        
        /** POST /api/kids/checkout - Check out a child from a service */
        val checkOutChild = KidsController::checkOutChild
        
        /** GET /api/kids/checkins/current - Get current check-ins */
        val getCurrentCheckIns = KidsController::getCurrentCheckIns
        
        /** GET /api/kids/checkins/history - Get check-in history */
        val getCheckInHistory = KidsController::getCheckInHistory
    }

    /**
     * QR Code Check-In Request Endpoints
     * Base Path: /api/kids/checkin-requests
     */
    object CheckInRequests {
        /** POST /api/kids/checkin-requests - Create a new check-in request with QR code */
        val createCheckInRequest = CheckInRequestController::createCheckInRequest
        
        /** GET /api/kids/checkin-requests/token/{token} - Get check-in request details by QR token (STAFF/ADMIN) */
        val getRequestByToken = CheckInRequestController::getRequestByToken
        
        /** POST /api/kids/checkin-requests/token/{token}/approve - Approve check-in request (STAFF/ADMIN) */
        val approveCheckIn = CheckInRequestController::approveCheckIn
        
        /** POST /api/kids/checkin-requests/token/{token}/reject - Reject check-in request (STAFF/ADMIN) */
        val rejectCheckIn = CheckInRequestController::rejectCheckIn
        
        /** DELETE /api/kids/checkin-requests/{requestId} - Cancel pending check-in request */
        val cancelRequest = CheckInRequestController::cancelRequest
        
        /** GET /api/kids/checkin-requests/active - Get all active check-in requests for current user */
        val getActiveRequests = CheckInRequestController::getActiveRequests
    }

    /**
     * Post Endpoints
     * Base Path: /api/posts
     */
    object Posts {
        /** POST /api/posts - Create a new post */
        val createPost = PostController::createPost
        
        /** GET /api/posts - Get all posts with pagination */
        val getAllPosts = PostController::getAllPosts
        
        /** GET /api/posts/{id} - Get specific post by ID */
        val getPostById = PostController::getPostById
        
        /** PUT /api/posts/{id} - Update an existing post */
        val updatePost = PostController::updatePost
        
        /** DELETE /api/posts/{id} - Delete a post */
        val deletePost = PostController::deletePost
        
        /** POST /api/posts/{id}/like - Like or unlike a post */
        val togglePostLike = PostController::togglePostLike
        
        /** GET /api/posts/author/{authorId} - Get posts by specific author */
        val getPostsByAuthor = PostController::getPostsByAuthor
        
        /** POST /api/posts/search - Search posts */
        val searchPosts = PostController::searchPosts
        
        /** POST /api/posts/{id}/comments - Add comment to a post */
        val addComment = PostController::addComment
        
        /** GET /api/posts/{id}/comments - Get comments for a post */
        val getPostComments = PostController::getPostComments
        
        /** DELETE /api/posts/comments/{commentId} - Delete a comment */
        val deleteComment = PostController::deleteComment
        
        /** GET /api/posts/stats - Get post statistics */
        val getPostStats = PostController::getPostStats
    }

    /**
     * Admin Endpoints
     * Base Path: /api/admin
     */
    object Admin {
        /** POST /api/admin/create-sunday-services - Trigger Sunday services creation job */
        val createSundayServices = AdminController::createSundayServices
        
        /** GET /api/admin/token - Get admin token for testing */
        val getAdminToken = AdminController::getAdminToken
    }

    /**
     * Profile Endpoints
     * Base Path: /api/profile
     */
    object Profile {
        /** GET /api/profile - Get current user's profile */
        val getCurrentUserProfile = ProfileController::getCurrentUserProfile
        
        /** GET /api/profile/{userId} - Get user profile by ID */
        val getUserProfile = ProfileController::getUserProfile
        
        /** PUT /api/profile - Update current user's profile */
        val updateCurrentUserProfile = ProfileController::updateCurrentUserProfile
        
        /** POST /api/profile/image - Upload profile image for current user */
        val uploadProfileImage = ProfileController::uploadProfileImage
        
        /** GET /api/profile/all - Get all user profiles (ADMIN) */
        val getAllUserProfiles = ProfileController::getAllUserProfiles
        
        /** GET /api/profile/search - Search user profiles (ADMIN) */
        val searchUserProfiles = ProfileController::searchUserProfiles
        
        /** GET /api/profile/admins - Get admin profiles (ADMIN) */
        val getAdminProfiles = ProfileController::getAdminProfiles
        
        /** PUT /api/profile/{userId}/admin-status - Update user admin status (ADMIN) */
        val updateUserAdminStatus = ProfileController::updateUserAdminStatus
        
        /** DELETE /api/profile/{userId} - Delete user account (ADMIN) */
        val deleteUser = ProfileController::deleteUser
    }

    /**
     * All Endpoints - Flat list for easy searching
     */
    object All {
        // Authentication
        val authLogin = Auth.login
        val authSignup = Auth.signup
        val authVerify = Auth.verify
        val authVerifyByToken = Auth.verifyByToken
        val authForgotPassword = Auth.forgotPassword
        val authResetPassword = Auth.resetPassword
        val authGoogleLogin = Auth.googleLogin
        val authFacebookLogin = Auth.facebookLogin
        val authRefreshToken = Auth.refreshToken
        val authLogout = Auth.logout

        // Attendance
        val attendanceCheckIn = Attendance.checkIn
        val attendanceCheckOut = Attendance.checkOut
        val attendanceGetMyAttendance = Attendance.getMyAttendance
        val attendanceGetUserAttendance = Attendance.getUserAttendance
        val attendanceGetEventAttendance = Attendance.getEventAttendance
        val attendanceGetServiceAttendance = Attendance.getServiceAttendance
        val attendanceGetKidsServiceAttendance = Attendance.getKidsServiceAttendance
        val attendanceGetCurrentlyCheckedIn = Attendance.getCurrentlyCheckedIn
        val attendanceGetAttendanceStats = Attendance.getAttendanceStats
        val attendanceGetMostFrequentAttendees = Attendance.getMostFrequentAttendees
        val attendanceBulkCheckIn = Attendance.bulkCheckIn
        val attendanceUpdateAttendanceStatus = Attendance.updateAttendanceStatus
        val attendanceGetAttendanceByType = Attendance.getAttendanceByType
        val attendanceGetAttendanceByStatus = Attendance.getAttendanceByStatus

        // Events
        val eventsGetAllEvents = Events.getAllEvents
        val eventsGetUpcomingEvents = Events.getUpcomingEvents
        val eventsGetEventById = Events.getEventById
        val eventsCreateEvent = Events.createEvent
        val eventsUpdateEvent = Events.updateEvent
        val eventsDeleteEvent = Events.deleteEvent
        val eventsJoinEvent = Events.joinEvent
        val eventsLeaveEvent = Events.leaveEvent
        val eventsApproveUserForEvent = Events.approveUserForEvent
        val eventsGetUserEventStatus = Events.getUserEventStatus
        val eventsGetMyEvents = Events.getMyEvents
        val eventsGetEventsAttending = Events.getEventsAttending
        val eventsGetEventsOnWaitingList = Events.getEventsOnWaitingList
        val eventsSearchEvents = Events.searchEvents

        // Files
        val filesServeFile = Files.serveFile
        val filesServeRootFile = Files.serveRootFile
        val filesGetFileInfo = Files.getFileInfo

        // Health
        val healthCheck = Health.health

        // Kids
        val kidsGetServices = Kids.getServices
        val kidsGetService = Kids.getService
        val kidsRegisterChild = Kids.registerChild
        val kidsGetChildren = Kids.getChildren
        val kidsGetChildrenByParent = Kids.getChildrenByParent
        val kidsGetChild = Kids.getChild
        val kidsUpdateChild = Kids.updateChild
        val kidsDeleteChild = Kids.deleteChild
        val kidsCheckInChild = Kids.checkInChild
        val kidsCheckOutChild = Kids.checkOutChild
        val kidsGetCurrentCheckIns = Kids.getCurrentCheckIns
        val kidsGetCheckInHistory = Kids.getCheckInHistory

        // QR Code Check-In Requests
        val checkInRequestsCreate = CheckInRequests.createCheckInRequest
        val checkInRequestsGetByToken = CheckInRequests.getRequestByToken
        val checkInRequestsApprove = CheckInRequests.approveCheckIn
        val checkInRequestsReject = CheckInRequests.rejectCheckIn
        val checkInRequestsCancel = CheckInRequests.cancelRequest
        val checkInRequestsGetActive = CheckInRequests.getActiveRequests

        // Posts
        val postsCreatePost = Posts.createPost
        val postsGetAllPosts = Posts.getAllPosts
        val postsGetPostById = Posts.getPostById
        val postsUpdatePost = Posts.updatePost
        val postsDeletePost = Posts.deletePost
        val postsTogglePostLike = Posts.togglePostLike
        val postsGetPostsByAuthor = Posts.getPostsByAuthor
        val postsSearchPosts = Posts.searchPosts
        val postsAddComment = Posts.addComment
        val postsGetPostComments = Posts.getPostComments
        val postsDeleteComment = Posts.deleteComment
        val postsGetPostStats = Posts.getPostStats

        // Admin
        val adminCreateSundayServices = Admin.createSundayServices
        val adminGetAdminToken = Admin.getAdminToken

        // Profile
        val profileGetCurrentUserProfile = Profile.getCurrentUserProfile
        val profileGetUserProfile = Profile.getUserProfile
        val profileUpdateCurrentUserProfile = Profile.updateCurrentUserProfile
        val profileUploadProfileImage = Profile.uploadProfileImage
        val profileGetAllUserProfiles = Profile.getAllUserProfiles
        val profileSearchUserProfiles = Profile.searchUserProfiles
        val profileGetAdminProfiles = Profile.getAdminProfiles
        val profileUpdateUserAdminStatus = Profile.updateUserAdminStatus
        val profileDeleteUser = Profile.deleteUser
    }

    /**
     * Endpoint Statistics
     */
    object Stats {
        const val TOTAL_ENDPOINTS = 77
        const val AUTH_ENDPOINTS = 10
        const val ATTENDANCE_ENDPOINTS = 14
        const val EVENT_ENDPOINTS = 14
        const val FILE_ENDPOINTS = 3
        const val HEALTH_ENDPOINTS = 1
        const val KIDS_ENDPOINTS = 13
        const val CHECKIN_REQUEST_ENDPOINTS = 6
        const val POST_ENDPOINTS = 12
        const val ADMIN_ENDPOINTS = 2
        const val PROFILE_ENDPOINTS = 9
    }
}