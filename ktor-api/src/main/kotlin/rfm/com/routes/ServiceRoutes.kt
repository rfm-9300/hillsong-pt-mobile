package rfm.com.routes

import rfm.com.data.db.service.Service
import rfm.com.data.db.service.ServiceRepository
import rfm.com.data.db.service.ServiceType
import rfm.com.data.responses.ApiResponseData
import rfm.com.plugins.Logger
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Route.serviceRoutes(
    serviceRepository: ServiceRepository
) {
    /**
     * API Routes for Services
     */

    // Get all services
    get(Routes.Api.Service.LIST) {
        try {
            val services = serviceRepository.getAllServices()
            Logger.d("Services fetched successfully: ${services.size} services found")
            respondHelper(
                call = call,
                success = true,
                message = "Services fetched successfully",
                data = ApiResponseData.ServiceListResponse(services)
            )
        } catch (e: Exception) {
            Logger.d("Error fetching services: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = "Error fetching services: ${e.message}",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }

    // Get active services only
    get(Routes.Api.Service.ACTIVE) {
        try {
            val services = serviceRepository.getActiveServices()
            Logger.d("Active services fetched successfully: ${services.size} services found")
            respondHelper(
                call = call,
                success = true,
                message = "Active services fetched successfully",
                data = ApiResponseData.ServiceListResponse(services)
            )
        } catch (e: Exception) {
            Logger.d("Error fetching active services: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = "Error fetching active services: ${e.message}",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }

    // Get service by ID
    get(Routes.Api.Service.GET) {
        try {
            val serviceId = call.parameters["id"]?.toIntOrNull()
            if (serviceId == null) {
                return@get respondHelper(
                    call = call,
                    success = false,
                    message = "Invalid service ID",
                    statusCode = HttpStatusCode.BadRequest
                )
            }

            val service = serviceRepository.getServiceById(serviceId)
            if (service != null) {
                Logger.d("Service fetched successfully: $service")
                respondHelper(
                    call = call,
                    success = true,
                    message = "Service fetched successfully",
                    data = ApiResponseData.SingleServiceResponse(service)
                )
            } else {
                respondHelper(
                    call = call,
                    success = false,
                    message = "Service not found",
                    statusCode = HttpStatusCode.NotFound
                )
            }
        } catch (e: Exception) {
            Logger.d("Error fetching service: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = "Error fetching service: ${e.message}",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }

    // Get services by type
    get(Routes.Api.Service.BY_TYPE) {
        try {
            val serviceTypeParam = call.parameters["type"]
            if (serviceTypeParam == null) {
                return@get respondHelper(
                    call = call,
                    success = false,
                    message = "Service type is required",
                    statusCode = HttpStatusCode.BadRequest
                )
            }

            val serviceType = try {
                ServiceType.valueOf(serviceTypeParam.uppercase())
            } catch (e: IllegalArgumentException) {
                return@get respondHelper(
                    call = call,
                    success = false,
                    message = "Invalid service type. Valid types: ${ServiceType.values().joinToString()}",
                    statusCode = HttpStatusCode.BadRequest
                )
            }

            val services = serviceRepository.getServicesByType(serviceType)
            Logger.d("Services by type $serviceType fetched successfully: ${services.size} services found")
            respondHelper(
                call = call,
                success = true,
                message = "Services fetched successfully",
                data = ApiResponseData.ServiceListResponse(services)
            )
        } catch (e: Exception) {
            Logger.d("Error fetching services by type: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = "Error fetching services by type: ${e.message}",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }

    // Create service (Admin only)
    authenticate {
        post(Routes.Api.Service.CREATE) {
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

                val request = call.receive<CreateServiceRequest>()
                
                val startTime = try {
                    LocalDateTime.parse(request.startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (e: Exception) {
                    return@post respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid start time format. Use ISO format: yyyy-MM-ddTHH:mm:ss",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val endTime = try {
                    LocalDateTime.parse(request.endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (e: Exception) {
                    return@post respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid end time format. Use ISO format: yyyy-MM-ddTHH:mm:ss",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val serviceType = try {
                    ServiceType.valueOf(request.serviceType.uppercase())
                } catch (e: IllegalArgumentException) {
                    return@post respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid service type. Valid types: ${ServiceType.values().joinToString()}",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val service = Service(
                    name = request.name,
                    description = request.description,
                    startTime = startTime,
                    endTime = endTime,
                    location = request.location,
                    serviceType = serviceType,
                    isActive = request.isActive
                )

                val createdService = serviceRepository.createService(service)
                if (createdService != null) {
                    Logger.d("Service created successfully: $createdService")
                    respondHelper(
                        call = call,
                        success = true,
                        message = "Service created successfully",
                        data = ApiResponseData.SingleServiceResponse(createdService),
                        statusCode = HttpStatusCode.Created
                    )
                } else {
                    respondHelper(
                        call = call,
                        success = false,
                        message = "Failed to create service",
                        statusCode = HttpStatusCode.InternalServerError
                    )
                }
            } catch (e: Exception) {
                Logger.d("Error creating service: ${e.message}")
                respondHelper(
                    call = call,
                    success = false,
                    message = "Error creating service: ${e.message}",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }

    // Update service (Admin only)
    authenticate {
        put(Routes.Api.Service.UPDATE) {
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

                val serviceId = call.parameters["id"]?.toIntOrNull()
                if (serviceId == null) {
                    return@put respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid service ID",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val request = call.receive<UpdateServiceRequest>()
                
                val startTime = try {
                    LocalDateTime.parse(request.startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (e: Exception) {
                    return@put respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid start time format. Use ISO format: yyyy-MM-ddTHH:mm:ss",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val endTime = try {
                    LocalDateTime.parse(request.endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (e: Exception) {
                    return@put respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid end time format. Use ISO format: yyyy-MM-ddTHH:mm:ss",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val serviceType = try {
                    ServiceType.valueOf(request.serviceType.uppercase())
                } catch (e: IllegalArgumentException) {
                    return@put respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid service type. Valid types: ${ServiceType.values().joinToString()}",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val service = Service(
                    id = serviceId,
                    name = request.name,
                    description = request.description,
                    startTime = startTime,
                    endTime = endTime,
                    location = request.location,
                    serviceType = serviceType,
                    isActive = request.isActive
                )

                val updated = serviceRepository.updateService(service)
                if (updated) {
                    Logger.d("Service updated successfully")
                    respondHelper(
                        call = call,
                        success = true,
                        message = "Service updated successfully"
                    )
                } else {
                    respondHelper(
                        call = call,
                        success = false,
                        message = "Failed to update service or service not found",
                        statusCode = HttpStatusCode.NotFound
                    )
                }
            } catch (e: Exception) {
                Logger.d("Error updating service: ${e.message}")
                respondHelper(
                    call = call,
                    success = false,
                    message = "Error updating service: ${e.message}",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }

    // Delete service (Admin only)
    authenticate {
        delete(Routes.Api.Service.DELETE) {
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

                val serviceId = call.parameters["id"]?.toIntOrNull()
                if (serviceId == null) {
                    return@delete respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid service ID",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val deleted = serviceRepository.deleteService(serviceId)
                if (deleted) {
                    Logger.d("Service deleted successfully")
                    respondHelper(
                        call = call,
                        success = true,
                        message = "Service deleted successfully"
                    )
                } else {
                    respondHelper(
                        call = call,
                        success = false,
                        message = "Failed to delete service or service not found",
                        statusCode = HttpStatusCode.NotFound
                    )
                }
            } catch (e: Exception) {
                Logger.d("Error deleting service: ${e.message}")
                respondHelper(
                    call = call,
                    success = false,
                    message = "Error deleting service: ${e.message}",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }

    // Activate service (Admin only)
    authenticate {
        post(Routes.Api.Service.ACTIVATE) {
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

                val serviceId = call.parameters["id"]?.toIntOrNull()
                if (serviceId == null) {
                    return@post respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid service ID",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val activated = serviceRepository.activateService(serviceId)
                if (activated) {
                    Logger.d("Service activated successfully")
                    respondHelper(
                        call = call,
                        success = true,
                        message = "Service activated successfully"
                    )
                } else {
                    respondHelper(
                        call = call,
                        success = false,
                        message = "Failed to activate service or service not found",
                        statusCode = HttpStatusCode.NotFound
                    )
                }
            } catch (e: Exception) {
                Logger.d("Error activating service: ${e.message}")
                respondHelper(
                    call = call,
                    success = false,
                    message = "Error activating service: ${e.message}",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }

    // Deactivate service (Admin only)
    authenticate {
        post(Routes.Api.Service.DEACTIVATE) {
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

                val serviceId = call.parameters["id"]?.toIntOrNull()
                if (serviceId == null) {
                    return@post respondHelper(
                        call = call,
                        success = false,
                        message = "Invalid service ID",
                        statusCode = HttpStatusCode.BadRequest
                    )
                }

                val deactivated = serviceRepository.deactivateService(serviceId)
                if (deactivated) {
                    Logger.d("Service deactivated successfully")
                    respondHelper(
                        call = call,
                        success = true,
                        message = "Service deactivated successfully"
                    )
                } else {
                    respondHelper(
                        call = call,
                        success = false,
                        message = "Failed to deactivate service or service not found",
                        statusCode = HttpStatusCode.NotFound
                    )
                }
            } catch (e: Exception) {
                Logger.d("Error deactivating service: ${e.message}")
                respondHelper(
                    call = call,
                    success = false,
                    message = "Error deactivating service: ${e.message}",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }
}

// Request data classes
@kotlinx.serialization.Serializable
data class CreateServiceRequest(
    val name: String,
    val description: String = "",
    val startTime: String, // ISO format: yyyy-MM-ddTHH:mm:ss
    val endTime: String,   // ISO format: yyyy-MM-ddTHH:mm:ss
    val location: String,
    val serviceType: String, // REGULAR, SPECIAL, YOUTH, KIDS, PRAYER, WORSHIP
    val isActive: Boolean = true
)

@kotlinx.serialization.Serializable
data class UpdateServiceRequest(
    val name: String,
    val description: String = "",
    val startTime: String, // ISO format: yyyy-MM-ddTHH:mm:ss
    val endTime: String,   // ISO format: yyyy-MM-ddTHH:mm:ss
    val location: String,
    val serviceType: String, // REGULAR, SPECIAL, YOUTH, KIDS, PRAYER, WORSHIP
    val isActive: Boolean = true
)