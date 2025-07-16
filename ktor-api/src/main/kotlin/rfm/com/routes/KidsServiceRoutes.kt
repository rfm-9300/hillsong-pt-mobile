package rfm.com.routes

import rfm.com.data.db.kidsservice.KidsService
import rfm.com.data.db.kidsservice.KidsCheckIn
import rfm.com.data.db.kidsservice.KidsServiceRepository
import rfm.com.data.db.kidsservice.CheckInStatus
import rfm.com.data.responses.ApiResponseData
import rfm.com.plugins.Logger
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime

fun Route.kidsServiceRoutes(
    kidsServiceRepository: KidsServiceRepository
) {
    /**
     * API Routes for Kids Services
     */

    // Get all kids services
    get(Routes.Api.KidsService.LIST) {
        try {
            val kidsServices = kidsServiceRepository.getAllKidsServices()
            Logger.d("Kids services fetched successfully: ${kidsServices.size} services found")
            respondHelper(
                call = call,
                success = true,
                message = "Kids services fetched successfully",
                data = ApiResponseData.KidsServiceListResponse(kidsServices)
            )
        } catch (e: Exception) {
            Logger.d("Error fetching kids services: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = "Error fetching kids services: ${e.message}",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }

    // Get active kids services only
    get(Routes.Api.KidsService.ACTIVE) {
        try {
            val kidsServices = kidsServiceRepository.getActiveKidsServices()
            Logger.d("Active kids services fetched successfully: ${kidsServices.size} services found")
            respondHelper(
                call = call,
                success = true,
                message = "Active kids services fetched successfully",
                data = ApiResponseData.KidsServiceListResponse(kidsServices)
            )
        } catch (e: Exception) {
            Logger.d("Error fetching active kids services: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = "Error fetching active kids services: ${e.message}",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }

    // Get kids service by ID
    get(Routes.Api.KidsService.GET) {
        try {
            val kidsServiceId = call.parameters["id"]?.toIntOrNull()
            if (kidsServiceId == null) {
                return@get respondHelper(
                    call = call,
                    success = false,
                    message = "Invalid kids service ID",
                    statusCode = HttpStatusCode.BadRequest
                )
            }

            val kidsService = kidsServiceRepository.getKidsServiceById(kidsServiceId)
            if (kidsService != null) {
                Logger.d("Kids service fetched successfully: $kidsService")
                respondHelper(
                    call = call,
                    success = true,
                    message = "Kids service fetched successfully",
                    data = ApiResponseData.SingleKidsServiceResponse(kidsService)
                )
            } else {
                respondHelper(
                    call = call,
                    success = false,
                    message = "Kids service not found",
                    statusCode = HttpStatusCode.NotFound
                )
            }
        } catch (e: Exception) {
            Logger.d("Error fetching kids service: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = "Error fetching kids service: ${e.message}",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }

    // Get kids services by main service ID
    get(Routes.Api.KidsService.BY_SERVICE) {
        try {
            val serviceId = call.parameters["serviceId"]?.toIntOrNull()
            if (serviceId == null) {
                return@get respondHelper(
                    call = call,
                    success = false,
                    message = "Invalid service ID",
                    statusCode = HttpStatusCode.BadRequest
                )
            }

            val kidsServices = kidsServiceRepository.getKidsServicesByServiceId(serviceId)
            Logger.d("Kids services by service ID $serviceId fetched successfully: ${kidsServices.size} services found")
            respondHelper(
                call = call,
                success = true,
                message = "Kids services fetched successfully",
                data = ApiResponseData.KidsServiceListResponse(kidsServices)
            )
        } catch (e: Exception) {
            Logger.d("Error fetching kids services by service ID: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = "Error fetching kids services by service ID: ${e.message}",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }

    // Create kids service (Admin only)
    authenticate {
        post(Routes.Api.KidsService.CREATE) {
            try {
                val userId = getUserIdFromRequestToken(call) ?: return@post respondHelper(
                    call = call,
                    success = false,
                    message = "User not found",
                    statusCode = HttpStatusCode.Unauthorized
                )

                if (!isUserAdmin(userId)) {
                    return@post respondHelper(
                        call = call,
                        success = false,
                        message = "Unauthorized - Admin access required",
                        statusCode = HttpStatusCode.Forbidden
                    )
                }

                val request = call.receive<CreateKidsServiceRequest>()

                val kidsService = KidsService(
                    serviceId = request.serviceId,
                    name = request.name,
                    description = request.description,
                    ageGroupMin = request.ageGroupMin,
                    ageGroupMax = request.ageGroupMax,
                    maxCapacity = request.maxCapacity,
                    location = request.location,
                    isActive = request.isActive
                )

                val createdKidsService = kidsServiceRepository.createKidsService(kidsService)
                if (createdKidsService != null) {
                    Logger.d("Kids service created successfully: $createdKidsService")
                    respondHelper(
                        call = call,
                        success = true,
                        message = "Kids service created successfully",
                        data = ApiResponseData.SingleKidsServiceResponse(createdKidsService),
                        statusCode = HttpStatusCode.Created
                    )
                } else {
                    respondHelper(
                        call = call,
                        success = false,
                        message = "Failed to create kids service",
                        statusCode = HttpStatusCode.InternalServerError
                    )
                }
            } catch (e: Exception) {
                Logger.d("Error creating kids service: ${e.message}")
                respondHelper(
                    call = call,
                    success = false,
                    message = "Error creating kids service: ${e.message}",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }

    // Update kids service (Admin only)
    authenticate {
        put(Routes.Api.KidsService.UPDATE) {
            try {
                val userId = getUserIdFromRequestToken(call) ?: return@put respondHelper(
                    call = call,
                    success = false,
                    message = "User not found",
                    statusCode = HttpStatusCode.Unauthorized
                )

                if (!isUserAdmin(userId)) {
                    return@put respondHelper(
                        call = call,
                        success = false,
                        message = "Unauthorized - Admin access required",
                        statusCode = HttpStatusCode.Forbidden
                    )
                }

                val kidsServiceId = call.parameters["id"]?.toIntOrNull()
                if (kidsServiceId == null) {
                    return@put respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid kids service ID",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val request = call.receive<UpdateKidsServiceRequest>()

                val kidsService = KidsService(
                    id = kidsServiceId,
                    serviceId = request.serviceId,
                    name = request.name,
                    description = request.description,
                    ageGroupMin = request.ageGroupMin,
                    ageGroupMax = request.ageGroupMax,
                    maxCapacity = request.maxCapacity,
                    location = request.location,
                    isActive = request.isActive
                )

                val updated = kidsServiceRepository.updateKidsService(kidsService)
                if (updated) {
                    Logger.d("Kids service updated successfully")
                    respondHelper(
                        call = call,
                        success = true,
                        message = "Kids service updated successfully"
                    )
                } else {
                    respondHelper(
                        call = call,
                        success = false,
                        message = "Failed to update kids service or service not found",
                        statusCode = HttpStatusCode.NotFound
                    )
                }
            } catch (e: Exception) {
                Logger.d("Error updating kids service: ${e.message}")
                respondHelper(
                    call = call,
                    success = false,
                    message = "Error updating kids service: ${e.message}",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }

    // Delete kids service (Admin only)
    authenticate {
        delete(Routes.Api.KidsService.DELETE) {
            try {
                val userId = getUserIdFromRequestToken(call) ?: return@delete respondHelper(
                    call = call,
                    success = false,
                    message = "User not found",
                    statusCode = HttpStatusCode.Unauthorized
                )

                if (!isUserAdmin(userId)) {
                    return@delete respondHelper(
                        call = call,
                        success = false,
                        message = "Unauthorized - Admin access required",
                        statusCode = HttpStatusCode.Forbidden
                    )
                }

                val kidsServiceId = call.parameters["id"]?.toIntOrNull()
                if (kidsServiceId == null) {
                    return@delete respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid kids service ID",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val deleted = kidsServiceRepository.deleteKidsService(kidsServiceId)
                if (deleted) {
                    Logger.d("Kids service deleted successfully")
                    respondHelper(
                        call = call,
                        success = true,
                        message = "Kids service deleted successfully"
                    )
                } else {
                    respondHelper(
                        call = call,
                        success = false,
                        message = "Failed to delete kids service or service not found",
                        statusCode = HttpStatusCode.NotFound
                    )
                }
            } catch (e: Exception) {
                Logger.d("Error deleting kids service: ${e.message}")
                respondHelper(
                    call = call,
                    success = false,
                    message = "Error deleting kids service: ${e.message}",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }

    // Get kids service capacity
    get(Routes.Api.KidsService.CAPACITY) {
        try {
            val kidsServiceId = call.parameters["id"]?.toIntOrNull()
            if (kidsServiceId == null) {
                return@get respondHelper(
                    call = call,
                    success = false,
                    message = "Invalid kids service ID",
                    statusCode = HttpStatusCode.BadRequest
                )
            }

            val (currentCount, maxCapacity) = kidsServiceRepository.getKidsServiceCapacity(kidsServiceId)
            Logger.d("Kids service capacity fetched: $currentCount/$maxCapacity")
            respondHelper(
                call = call,
                success = true,
                message = "Kids service capacity fetched successfully",
                data = ApiResponseData.KidsServiceCapacityResponse(currentCount, maxCapacity, kidsServiceId)
            )
        } catch (e: Exception) {
            Logger.d("Error fetching kids service capacity: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = "Error fetching kids service capacity: ${e.message}",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }

    /**
     * Kids Check-in Routes
     */

    // Check in a kid
    authenticate {
        post(Routes.Api.KidsService.CHECK_IN) {
            try {
                val userId = getUserIdFromRequestToken(call) ?: return@post respondHelper(
                    call = call,
                    success = false,
                    message = "User not found",
                    statusCode = HttpStatusCode.Unauthorized
                )

                val request = call.receive<CheckInKidRequest>()

                val kidsCheckIn = KidsCheckIn(
                    kidsServiceId = request.kidsServiceId,
                    kidId = request.kidId,
                    checkedInBy = userId.toInt(),
                    notes = request.notes
                )

                val checkedIn = kidsServiceRepository.checkInKid(kidsCheckIn)
                if (checkedIn != null) {
                    Logger.d("Kid checked in successfully: $checkedIn")
                    respondHelper(
                        call = call,
                        success = true,
                        message = "Kid checked in successfully",
                        data = ApiResponseData.SingleKidsCheckInResponse(checkedIn),
                        statusCode = HttpStatusCode.Created
                    )
                } else {
                    respondHelper(
                        call = call,
                        success = false,
                        message = "Failed to check in kid. Kid may already be checked in.",
                        statusCode = HttpStatusCode.Conflict
                    )
                }
            } catch (e: Exception) {
                Logger.d("Error checking in kid: ${e.message}")
                respondHelper(
                    call = call,
                    success = false,
                    message = "Error checking in kid: ${e.message}",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }

    // Check out a kid
    authenticate {
        post(Routes.Api.KidsService.CHECK_OUT) {
            try {
                val userId = getUserIdFromRequestToken(call) ?: return@post respondHelper(
                    call = call,
                    success = false,
                    message = "User not found",
                    statusCode = HttpStatusCode.Unauthorized
                )

                val request = call.receive<CheckOutKidRequest>()

                val checkedOut = kidsServiceRepository.checkOutKid(
                    checkInId = request.checkInId,
                    checkedOutBy = userId.toInt(),
                    notes = request.notes
                )

                if (checkedOut) {
                    Logger.d("Kid checked out successfully")
                    respondHelper(
                        call = call,
                        success = true,
                        message = "Kid checked out successfully"
                    )
                } else {
                    respondHelper(
                        call = call,
                        success = false,
                        message = "Failed to check out kid or check-in not found",
                        statusCode = HttpStatusCode.NotFound
                    )
                }
            } catch (e: Exception) {
                Logger.d("Error checking out kid: ${e.message}")
                respondHelper(
                    call = call,
                    success = false,
                    message = "Error checking out kid: ${e.message}",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }

    // Get active check-ins for a kids service
    get(Routes.Api.KidsService.ACTIVE_CHECKINS) {
        try {
            val kidsServiceId = call.parameters["id"]?.toIntOrNull()
            if (kidsServiceId == null) {
                return@get respondHelper(
                    call = call,
                    success = false,
                    message = "Invalid kids service ID",
                    statusCode = HttpStatusCode.BadRequest
                )
            }

            val activeCheckIns = kidsServiceRepository.getActiveCheckIns(kidsServiceId)
            Logger.d("Active check-ins fetched successfully: ${activeCheckIns.size} check-ins found")
            respondHelper(
                call = call,
                success = true,
                message = "Active check-ins fetched successfully",
                data = ApiResponseData.KidsCheckInListResponse(activeCheckIns)
            )
        } catch (e: Exception) {
            Logger.d("Error fetching active check-ins: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = "Error fetching active check-ins: ${e.message}",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }

    // Get check-in history for a kid
    get(Routes.Api.KidsService.CHECKIN_HISTORY) {
        try {
            val kidId = call.parameters["kidId"]?.toIntOrNull()
            if (kidId == null) {
                return@get respondHelper(
                    call = call,
                    success = false,
                    message = "Invalid kid ID",
                    statusCode = HttpStatusCode.BadRequest
                )
            }

            val checkInHistory = kidsServiceRepository.getCheckInHistory(kidId)
            Logger.d("Check-in history fetched successfully: ${checkInHistory.size} records found")
            respondHelper(
                call = call,
                success = true,
                message = "Check-in history fetched successfully",
                data = ApiResponseData.KidsCheckInListResponse(checkInHistory)
            )
        } catch (e: Exception) {
            Logger.d("Error fetching check-in history: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = "Error fetching check-in history: ${e.message}",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }

    // Update check-in status (for emergency situations)
    authenticate {
        put(Routes.Api.KidsService.UPDATE_CHECKIN_STATUS) {
            try {
                val userId = getUserIdFromRequestToken(call) ?: return@put respondHelper(
                    call = call,
                    success = false,
                    message = "User not found",
                    statusCode = HttpStatusCode.Unauthorized
                )

                if (!isUserAdmin(userId)) {
                    return@put respondHelper(
                        call = call,
                        success = false,
                        message = "Unauthorized - Admin access required",
                        statusCode = HttpStatusCode.Forbidden
                    )
                }

                val checkInId = call.parameters["id"]?.toIntOrNull()
                if (checkInId == null) {
                    return@put respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid check-in ID",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val request = call.receive<UpdateCheckInStatusRequest>()
                
                val status = try {
                    CheckInStatus.valueOf(request.status.uppercase())
                } catch (e: IllegalArgumentException) {
                    return@put respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid status. Valid statuses: ${CheckInStatus.values().joinToString()}",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val updated = kidsServiceRepository.updateCheckInStatus(checkInId, status)
                if (updated) {
                    Logger.d("Check-in status updated successfully")
                    respondHelper(
                        call = call,
                        success = true,
                        message = "Check-in status updated successfully"
                    )
                } else {
                    respondHelper(
                        call = call,
                        success = false,
                        message = "Failed to update check-in status or check-in not found",
                        statusCode = HttpStatusCode.NotFound
                    )
                }
            } catch (e: Exception) {
                Logger.d("Error updating check-in status: ${e.message}")
                respondHelper(
                    call = call,
                    success = false,
                    message = "Error updating check-in status: ${e.message}",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }
}

// Request data classes
@kotlinx.serialization.Serializable
data class CreateKidsServiceRequest(
    val serviceId: Int,
    val name: String,
    val description: String = "",
    val ageGroupMin: Int,
    val ageGroupMax: Int,
    val maxCapacity: Int,
    val location: String,
    val isActive: Boolean = true
)

@kotlinx.serialization.Serializable
data class UpdateKidsServiceRequest(
    val serviceId: Int,
    val name: String,
    val description: String = "",
    val ageGroupMin: Int,
    val ageGroupMax: Int,
    val maxCapacity: Int,
    val location: String,
    val isActive: Boolean = true
)

@kotlinx.serialization.Serializable
data class CheckInKidRequest(
    val kidsServiceId: Int,
    val kidId: Int,
    val notes: String = ""
)

@kotlinx.serialization.Serializable
data class CheckOutKidRequest(
    val checkInId: Int,
    val notes: String = ""
)

@kotlinx.serialization.Serializable
data class UpdateCheckInStatusRequest(
    val status: String // CHECKED_IN, CHECKED_OUT, EMERGENCY
)