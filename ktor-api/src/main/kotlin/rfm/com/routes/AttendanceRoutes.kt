package rfm.com.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import rfm.com.data.db.attendance.AttendanceStatus
import rfm.com.plugins.Logger
import rfm.com.data.responses.ApiResponse
import rfm.com.data.responses.ApiResponseData
import rfm.com.services.AttendanceService

data class CheckInRequest(
    val userId: Int? = null,
    val kidId: Int? = null,
    val checkedInBy: Int,
    val notes: String = ""
)

data class CheckOutRequest(val attendanceId: Int, val checkedOutBy: Int, val notes: String = "")

data class UpdateAttendanceStatusRequest(
    val attendanceId: Int,
    val status: String,
    val notes: String = ""
)

data class UpdateAttendanceNotesRequest(val attendanceId: Int, val notes: String)

fun Route.attendanceRoutes(attendanceService: AttendanceService) {

    authenticate {
        route("/api/attendance") {

            // Check-in routes
            post("/event/{eventId}/check-in") {
                val eventId = call.parameters["eventId"]?.toIntOrNull()
                if (eventId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid event ID")
                    )
                    return@post
                }

                val request = call.receive<CheckInRequest>()

                if (request.userId != null) {
                    val attendance =
                        attendanceService.checkInUserToEvent(
                            eventId = eventId,
                            userId = request.userId,
                            checkedInBy = request.checkedInBy,
                            notes = request.notes
                        )

                    if (attendance != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse(success = true, data = ApiResponseData.AttendanceResponse(attendance))
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse(success = false, message = "Failed to check in user")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "User ID is required")
                    )
                }
            }

            post("/service/{serviceId}/check-in") {
                val serviceId = call.parameters["serviceId"]?.toIntOrNull()
                if (serviceId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid service ID")
                    )
                    return@post
                }

                val request = call.receive<CheckInRequest>()

                if (request.userId != null) {
                    val attendance =
                        attendanceService.checkInUserToService(
                            serviceId = serviceId,
                            userId = request.userId,
                            checkedInBy = request.checkedInBy,
                            notes = request.notes
                        )

                    if (attendance != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse(success = true, data = ApiResponseData.AttendanceResponse(attendance), message = "User checked in successfully")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse(success = false, message = "Failed to check in user")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "User ID is required")
                    )
                }
            }

            post("/kids-service/{kidsServiceId}/check-in") {
                val kidsServiceId = call.parameters["kidsServiceId"]?.toIntOrNull()
                if (kidsServiceId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid kids service ID")
                    )
                    return@post
                }

                val request = call.receive<CheckInRequest>()

                if (request.kidId != null) {
                    val attendance =
                        attendanceService.checkInKidToKidsService(
                            kidsServiceId = kidsServiceId,
                            kidId = request.kidId,
                            checkedInBy = request.checkedInBy,
                            notes = request.notes
                        )

                    if (attendance != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse(success = true, data = ApiResponseData.AttendanceResponse(attendance))
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse(success = false, message = "Failed to check in kid")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Kid ID is required")
                    )
                }
            }

            // Check-out routes
            post("/check-out") {
                val request = call.receive<CheckOutRequest>()

                val success =
                    attendanceService.checkOutUser(
                        attendanceId = request.attendanceId,
                        checkedOutBy = request.checkedOutBy,
                        notes = request.notes
                    )

                if (success) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "Successfully checked out")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(success = false, message = "Failed to check out")
                    )
                }
            }

            post("/kid/check-out") {
                val request = call.receive<CheckOutRequest>()

                val success =
                    attendanceService.checkOutKid(
                        attendanceId = request.attendanceId,
                        checkedOutBy = request.checkedOutBy,
                        notes = request.notes
                    )

                if (success) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "Successfully checked out kid")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(success = false, message = "Failed to check out kid")
                    )
                }
            }

            // Query routes
            get("/event/{eventId}") {
                Logger.d("Fetching attendance for event")
                val eventId = call.parameters["eventId"]?.toIntOrNull()
                if (eventId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid event ID")
                    )
                    return@get
                }

                val attendances = attendanceService.getEventAttendance(eventId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = ApiResponseData.AttendanceListResponse(attendances)))
            }

            get("/service/{serviceId}") {
                Logger.d("Fetching attendance for service")
                val serviceId = call.parameters["serviceId"]?.toIntOrNull()
                if (serviceId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid service ID")
                    )
                    return@get
                }

                val attendances = attendanceService.getServiceAttendance(serviceId)
                Logger.d("Fetched ${attendances.size} attendances for service $serviceId")
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = ApiResponseData.AttendanceListResponse(attendances)))
            }

            get("/kids-service/{kidsServiceId}") {
                val kidsServiceId = call.parameters["kidsServiceId"]?.toIntOrNull()
                if (kidsServiceId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid kids service ID")
                    )
                    return@get
                }

                val attendances = attendanceService.getKidsServiceAttendance(kidsServiceId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = ApiResponseData.AttendanceListResponse(attendances)))
            }

            get("/user/{userId}") {
                val userId = call.parameters["userId"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid user ID")
                    )
                    return@get
                }

                val startDateStr = call.request.queryParameters["startDate"]
                val endDateStr = call.request.queryParameters["endDate"]

                val formatter = DateTimeFormatter.ISO_DATE_TIME
                val startDate = startDateStr?.let { LocalDateTime.parse(it, formatter) }
                val endDate = endDateStr?.let { LocalDateTime.parse(it, formatter) }

                val attendances =
                    attendanceService.getUserAttendanceHistory(userId, startDate, endDate)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = ApiResponseData.AttendanceListResponse(attendances)))
            }

            get("/kid/{kidId}") {
                val kidId = call.parameters["kidId"]?.toIntOrNull()
                if (kidId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid kid ID")
                    )
                    return@get
                }

                val startDateStr = call.request.queryParameters["startDate"]
                val endDateStr = call.request.queryParameters["endDate"]

                val formatter = DateTimeFormatter.ISO_DATE_TIME
                val startDate = startDateStr?.let { LocalDateTime.parse(it, formatter) }
                val endDate = endDateStr?.let { LocalDateTime.parse(it, formatter) }

                val attendances =
                    attendanceService.getKidAttendanceHistory(kidId, startDate, endDate)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = ApiResponseData.AttendanceListResponse(attendances)))
            }

            get("/event/{eventId}/current") {
                val eventId = call.parameters["eventId"]?.toIntOrNull()
                if (eventId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid event ID")
                    )
                    return@get
                }

                val attendances = attendanceService.getCurrentlyCheckedInToEvent(eventId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = ApiResponseData.AttendanceListResponse(attendances)))
            }

            get("/service/{serviceId}/current") {
                val serviceId = call.parameters["serviceId"]?.toIntOrNull()
                if (serviceId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid service ID")
                    )
                    return@get
                }

                val attendances = attendanceService.getCurrentlyCheckedInToService(serviceId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = ApiResponseData.AttendanceListResponse(attendances)))
            }

            get("/kids-service/{kidsServiceId}/current") {
                val kidsServiceId = call.parameters["kidsServiceId"]?.toIntOrNull()
                if (kidsServiceId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid kids service ID")
                    )
                    return@get
                }

                val attendances =
                    attendanceService.getCurrentlyCheckedInToKidsService(kidsServiceId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = ApiResponseData.AttendanceListResponse(attendances)))
            }

            get("/event/{eventId}/stats") {
                val eventId = call.parameters["eventId"]?.toIntOrNull()
                if (eventId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid event ID")
                    )
                    return@get
                }

                val stats = attendanceService.getEventAttendanceStats(eventId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = ApiResponseData.AttendanceStatsResponse(stats)))
            }

            get("/service/{serviceId}/stats") {
                val serviceId = call.parameters["serviceId"]?.toIntOrNull()
                if (serviceId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid service ID")
                    )
                    return@get
                }

                val stats = attendanceService.getServiceAttendanceStats(serviceId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = ApiResponseData.AttendanceStatsResponse(stats)))
            }

            get("/kids-service/{kidsServiceId}/stats") {
                val kidsServiceId = call.parameters["kidsServiceId"]?.toIntOrNull()
                if (kidsServiceId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid kids service ID")
                    )
                    return@get
                }

                val stats = attendanceService.getKidsServiceAttendanceStats(kidsServiceId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = ApiResponseData.AttendanceStatsResponse(stats)))
            }

            get("/event/{eventId}/user/{userId}/status") {
                val eventId = call.parameters["eventId"]?.toIntOrNull()
                val userId = call.parameters["userId"]?.toIntOrNull()

                if (eventId == null || userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid event ID or user ID")
                    )
                    return@get
                }

                val isCheckedIn = attendanceService.isUserCheckedInToEvent(eventId, userId)
                val responseData = mapOf("isCheckedIn" to isCheckedIn)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, data = ApiResponseData.AuthResponse(responseData.toString()))
                )
            }

            get("/service/{serviceId}/user/{userId}/status") {
                val serviceId = call.parameters["serviceId"]?.toIntOrNull()
                val userId = call.parameters["userId"]?.toIntOrNull()

                if (serviceId == null || userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid service ID or user ID")
                    )
                    return@get
                }

                val isCheckedIn = attendanceService.isUserCheckedInToService(serviceId, userId)
                val responseData = mapOf("isCheckedIn" to isCheckedIn)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, data = ApiResponseData.AuthResponse(responseData.toString()))
                )
            }

            get("/kids-service/{kidsServiceId}/kid/{kidId}/status") {
                val kidsServiceId = call.parameters["kidsServiceId"]?.toIntOrNull()
                val kidId = call.parameters["kidId"]?.toIntOrNull()

                if (kidsServiceId == null || kidId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(
                            success = false,
                            message = "Invalid kids service ID or kid ID"
                        )
                    )
                    return@get
                }

                val isCheckedIn =
                    attendanceService.isKidCheckedInToKidsService(kidsServiceId, kidId)
                val responseData = mapOf("isCheckedIn" to isCheckedIn)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, data = ApiResponseData.AuthResponse(responseData.toString()))
                )
            }

            // Update routes
            put("/status") {
                val request = call.receive<UpdateAttendanceStatusRequest>()

                try {
                    val status = AttendanceStatus.valueOf(request.status)
                    val success =
                        attendanceService.updateAttendanceStatus(
                            attendanceId = request.attendanceId,
                            status = status,
                            notes = request.notes
                        )

                    if (success) {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse(
                                success = true,
                                message = "Successfully updated attendance status"
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse(
                                success = false,
                                message = "Failed to update attendance status"
                            )
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, message = "Invalid attendance status")
                    )
                }
            }

            put("/notes") {
                val request = call.receive<UpdateAttendanceNotesRequest>()

                val success =
                    attendanceService.updateAttendanceNotes(
                        attendanceId = request.attendanceId,
                        notes = request.notes
                    )

                if (success) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "Successfully updated attendance notes"
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(
                            success = false,
                            message = "Failed to update attendance notes"
                        )
                    )
                }
            }
        }
    }
}