package rfm.com.controller

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import rfm.com.dto.*
import rfm.com.entity.AttendanceStatus
import rfm.com.entity.AttendanceType
import rfm.com.service.AttendanceService
import rfm.com.util.getCurrentUserId

@RestController
@RequestMapping("/api/attendance")
class AttendanceController(private val attendanceService: AttendanceService) {

        /** Check in to an event, service, or kids service */
        @PostMapping("/check-in")
        @PreAuthorize("hasRole('USER')")
        fun checkIn(
                @Valid @RequestBody request: CheckInRequest,
                authentication: Authentication
        ): ResponseEntity<ApiResponse<AttendanceResponse>> {
                return try {
                        val userId = authentication.getCurrentUserId()
                        val attendance = attendanceService.checkIn(userId, request)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message = "Successfully checked in",
                                        data = attendance
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "Invalid request"
                                        )
                                )
                } catch (e: IllegalStateException) {
                        ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "Conflict with current state"
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "An error occurred during check-in"
                                        )
                                )
                }
        }

        /** Check out from an attendance record */
        @PostMapping("/check-out")
        @PreAuthorize("hasRole('USER')")
        fun checkOut(
                @Valid @RequestBody request: CheckOutRequest,
                authentication: Authentication
        ): ResponseEntity<ApiResponse<AttendanceResponse>> {
                return try {
                        val userId = authentication.getCurrentUserId()
                        val attendance = attendanceService.checkOut(userId, request)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message = "Successfully checked out",
                                        data = attendance
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "Invalid request"
                                        )
                                )
                } catch (e: IllegalStateException) {
                        ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "Conflict with current state"
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "An error occurred during check-out"
                                        )
                                )
                }
        }

        /** Get attendance records for the current user */
        @GetMapping("/my-attendance")
        @PreAuthorize("hasRole('USER')")
        fun getMyAttendance(
                @RequestParam(defaultValue = "0") page: Int,
                @RequestParam(defaultValue = "20") size: Int,
                @RequestParam(defaultValue = "checkInTime") sortBy: String,
                @RequestParam(defaultValue = "desc") sortDir: String,
                authentication: Authentication
        ): ResponseEntity<ApiResponse<Page<AttendanceSummaryResponse>>> {
                return try {
                        val userId = authentication.getCurrentUserId()
                        val sort =
                                Sort.by(
                                        if (sortDir.lowercase() == "desc") Sort.Direction.DESC
                                        else Sort.Direction.ASC,
                                        sortBy
                                )
                        val pageable = PageRequest.of(page, size, sort)
                        val attendance = attendanceService.getUserAttendance(userId, pageable)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message = "Attendance records retrieved successfully",
                                        data = attendance
                                )
                        )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message =
                                                        "An error occurred while retrieving attendance records"
                                        )
                                )
                }
        }

        /** Get attendance records for a specific user (admin only) */
        @GetMapping("/user/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        fun getUserAttendance(
                @PathVariable userId: Long,
                @RequestParam(defaultValue = "0") page: Int,
                @RequestParam(defaultValue = "20") size: Int,
                @RequestParam(defaultValue = "checkInTime") sortBy: String,
                @RequestParam(defaultValue = "desc") sortDir: String
        ): ResponseEntity<ApiResponse<Page<AttendanceSummaryResponse>>> {
                return try {
                        val sort =
                                Sort.by(
                                        if (sortDir.lowercase() == "desc") Sort.Direction.DESC
                                        else Sort.Direction.ASC,
                                        sortBy
                                )
                        val pageable = PageRequest.of(page, size, sort)
                        val attendance = attendanceService.getUserAttendance(userId, pageable)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message = "User attendance records retrieved successfully",
                                        data = attendance
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "Invalid user ID"
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message =
                                                        "An error occurred while retrieving user attendance records"
                                        )
                                )
                }
        }

        /** Get attendance records for a specific event */
        @GetMapping("/event/{eventId}")
        @PreAuthorize("hasRole('USER')")
        fun getEventAttendance(
                @PathVariable eventId: Long
        ): ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> {
                return try {
                        val attendance = attendanceService.getEventAttendance(eventId)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message = "Event attendance records retrieved successfully",
                                        data = attendance
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "Invalid event ID"
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message =
                                                        "An error occurred while retrieving event attendance records"
                                        )
                                )
                }
        }

        /** Get attendance records for a specific service */
        @GetMapping("/service/{serviceId}")
        @PreAuthorize("hasRole('USER')")
        fun getServiceAttendance(
                @PathVariable serviceId: Long
        ): ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> {
                return try {
                        val attendance = attendanceService.getServiceAttendance(serviceId)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message =
                                                "Service attendance records retrieved successfully",
                                        data = attendance
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "Invalid service ID"
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message =
                                                        "An error occurred while retrieving service attendance records"
                                        )
                                )
                }
        }

        /** Get attendance records for a specific kids service */
        @GetMapping("/kids-service/{kidsServiceId}")
        @PreAuthorize("hasRole('USER')")
        fun getKidsServiceAttendance(
                @PathVariable kidsServiceId: Long
        ): ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> {
                return try {
                        val attendance = attendanceService.getKidsServiceAttendance(kidsServiceId)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message =
                                                "Kids service attendance records retrieved successfully",
                                        data = attendance
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "Invalid kids service ID"
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message =
                                                        "An error occurred while retrieving kids service attendance records"
                                        )
                                )
                }
        }

        /** Get currently checked-in users */
        @GetMapping("/currently-checked-in")
        @PreAuthorize("hasRole('USER')")
        fun getCurrentlyCheckedIn(): ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> {
                return try {
                        val attendance = attendanceService.getCurrentlyCheckedIn()

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message =
                                                "Currently checked-in users retrieved successfully",
                                        data = attendance
                                )
                        )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message =
                                                        "An error occurred while retrieving currently checked-in users"
                                        )
                                )
                }
        }

        /** Get attendance statistics and analytics */
        @PostMapping("/stats")
        @PreAuthorize("hasRole('USER')")
        fun getAttendanceStats(
                @Valid @RequestBody request: AttendanceReportRequest
        ): ResponseEntity<ApiResponse<AttendanceStatsResponse>> {
                return try {
                        val stats = attendanceService.getAttendanceStats(request)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message = "Attendance statistics retrieved successfully",
                                        data = stats
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "Invalid request parameters"
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message =
                                                        "An error occurred while retrieving attendance statistics"
                                        )
                                )
                }
        }

        /** Get most frequent attendees (admin only) */
        @GetMapping("/frequent-attendees")
        @PreAuthorize("hasRole('ADMIN')")
        fun getMostFrequentAttendees(
                @RequestParam(defaultValue = "0") page: Int,
                @RequestParam(defaultValue = "20") size: Int
        ): ResponseEntity<ApiResponse<Page<FrequentAttendeesResponse>>> {
                return try {
                        val pageable = PageRequest.of(page, size)
                        val frequentAttendees = attendanceService.getMostFrequentAttendees(pageable)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message = "Most frequent attendees retrieved successfully",
                                        data = frequentAttendees
                                )
                        )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message =
                                                        "An error occurred while retrieving frequent attendees"
                                        )
                                )
                }
        }

        /** Bulk check-in multiple users (admin only) */
        @PostMapping("/bulk-check-in")
        @PreAuthorize("hasRole('ADMIN')")
        fun bulkCheckIn(
                @Valid @RequestBody request: BulkCheckInRequest
        ): ResponseEntity<ApiResponse<BulkAttendanceResponse>> {
                return try {
                        val result = attendanceService.bulkCheckIn(request)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message =
                                                "Bulk check-in completed. ${result.successCount} successful, ${result.failureCount} failed.",
                                        data = result
                                )
                        )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = "An error occurred during bulk check-in"
                                        )
                                )
                }
        }

        /** Update attendance status (admin only) */
        @PutMapping("/{attendanceId}/status")
        @PreAuthorize("hasRole('ADMIN')")
        fun updateAttendanceStatus(
                @PathVariable attendanceId: Long,
                @RequestParam status: AttendanceStatus,
                @RequestParam(required = false) notes: String?
        ): ResponseEntity<ApiResponse<AttendanceResponse>> {
                return try {
                        val attendance =
                                attendanceService.updateAttendanceStatus(
                                        attendanceId,
                                        status,
                                        notes
                                )

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message = "Attendance status updated successfully",
                                        data = attendance
                                )
                        )
                } catch (e: IllegalArgumentException) {
                        ResponseEntity.badRequest()
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message = e.message ?: "Invalid request"
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message =
                                                        "An error occurred while updating attendance status"
                                        )
                                )
                }
        }

        /** Get attendance records by type */
        @GetMapping("/by-type/{type}")
        @PreAuthorize("hasRole('USER')")
        fun getAttendanceByType(
                @PathVariable type: AttendanceType
        ): ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> {
                return try {
                        val attendance = attendanceService.getAttendanceByType(type)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message =
                                                "Attendance records by type retrieved successfully",
                                        data = attendance
                                )
                        )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message =
                                                        "An error occurred while retrieving attendance records by type"
                                        )
                                )
                }
        }

        /** Get attendance records by status */
        @GetMapping("/by-status/{status}")
        @PreAuthorize("hasRole('USER')")
        fun getAttendanceByStatus(
                @PathVariable status: AttendanceStatus
        ): ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> {
                return try {
                        val attendance = attendanceService.getAttendanceByStatus(status)

                        ResponseEntity.ok(
                                ApiResponse(
                                        success = true,
                                        message =
                                                "Attendance records by status retrieved successfully",
                                        data = attendance
                                )
                        )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(
                                        ApiResponse(
                                                success = false,
                                                message =
                                                        "An error occurred while retrieving attendance records by status"
                                        )
                                )
                }
        }
}
