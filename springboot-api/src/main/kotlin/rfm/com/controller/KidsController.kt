package rfm.com.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import rfm.com.dto.*
import rfm.com.service.KidsService
import rfm.com.util.getCurrentUserId

@RestController
@RequestMapping("/api/kids")
class KidsController(private val kidsService: KidsService) {

    /** Get all available kids services */
    @GetMapping("/services")
    @PreAuthorize("hasRole('USER')")
    fun getServices(
        @RequestParam(required = false) minAge: Int?,
        @RequestParam(required = false) maxAge: Int?,
        @RequestParam(required = false) acceptingCheckIns: Boolean?,
        @RequestParam(required = false) location: String?
    ): ResponseEntity<ApiResponse<List<KidsServiceResponse>>> {
        return try {
            val services = kidsService.getAvailableServices(minAge, maxAge, acceptingCheckIns, location)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Kids services retrieved successfully",
                    data = services
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while retrieving kids services"
                    )
                )
        }
    }

    /** Get a specific kids service */
    @GetMapping("/services/{serviceId}")
    @PreAuthorize("hasRole('USER')")
    fun getService(@PathVariable serviceId: Long): ResponseEntity<ApiResponse<KidsServiceResponse>> {
        return try {
            val service = kidsService.getService(serviceId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Kids service retrieved successfully",
                    data = service
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse(success = false, message = e.message ?: "Invalid service ID"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while retrieving the kids service"
                    )
                )
        }
    }



    /** Register a new child */
    @PostMapping("/children")
    @PreAuthorize("hasRole('USER')")
    fun registerChild(
        @Valid @RequestBody request: ChildRegistrationRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<ChildResponse>> {
        return try {
            val userId = authentication.getCurrentUserId()
            val child = kidsService.registerChild(userId, request)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Child registered successfully",
                    data = child
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse(success = false, message = e.message ?: "Invalid request"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while registering the child"
                    )
                )
        }
    }

    /** Get all children */
    @GetMapping("/children")
    @PreAuthorize("hasRole('USER')")
    fun getChildren(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<List<ChildResponse>>> {
        return try {
            val userId = authentication.getCurrentUserId()
            val children = kidsService.getChildrenForParent(userId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Children retrieved successfully",
                    data = children
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while retrieving children"
                    )
                )
        }
    }

    /** Get children for a specific parent */
    @GetMapping("/children/parent/{parentId}")
    @PreAuthorize("hasRole('USER')")
    fun getChildrenByParent(
        @PathVariable parentId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<List<ChildResponse>>> {
        return try {
            val currentUserId = authentication.getCurrentUserId()
            // Users can only see their own children unless they are admin
            if (currentUserId != parentId && !authentication.authorities.any { it.authority == "ROLE_ADMIN" }) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse(success = false, message = "Access denied"))
            }
            
            val children = kidsService.getChildrenForParent(parentId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Children retrieved successfully",
                    data = children
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while retrieving children"
                    )
                )
        }
    }

    /** Get a specific child */
    @GetMapping("/children/{childId}")
    @PreAuthorize("hasRole('USER')")
    fun getChild(
        @PathVariable childId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<ChildResponse>> {
        return try {
            val userId = authentication.getCurrentUserId()
            val child = kidsService.getChild(childId, userId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Child retrieved successfully",
                    data = child
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse(success = false, message = e.message ?: "Invalid child ID"))
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(success = false, message = "Access denied"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while retrieving the child"
                    )
                )
        }
    }

    /** Update a child */
    @PutMapping("/children/{childId}")
    @PreAuthorize("hasRole('USER')")
    fun updateChild(
        @PathVariable childId: Long,
        @Valid @RequestBody request: ChildUpdateRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<ChildResponse>> {
        return try {
            val userId = authentication.getCurrentUserId()
            val child = kidsService.updateChild(childId, userId, request)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Child updated successfully",
                    data = child
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse(success = false, message = e.message ?: "Invalid request"))
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(success = false, message = "Access denied"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while updating the child"
                    )
                )
        }
    }

    /** Delete a child */
    @DeleteMapping("/children/{childId}")
    @PreAuthorize("hasRole('USER')")
    fun deleteChild(
        @PathVariable childId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Unit>> {
        return try {
            val userId = authentication.getCurrentUserId()
            kidsService.deleteChild(childId, userId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Child deleted successfully",
                    data = Unit
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse(success = false, message = e.message ?: "Invalid child ID"))
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(success = false, message = "Access denied"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while deleting the child"
                    )
                )
        }
    }

    /** Check in a child to a service */
    @PostMapping("/checkin")
    @PreAuthorize("hasRole('USER')")
    fun checkInChild(
        @Valid @RequestBody request: KidsCheckInRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<KidsCheckInResponse>> {
        return try {
            val userId = authentication.getCurrentUserId()
            val checkIn = kidsService.checkInChild(userId, request)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Child checked in successfully",
                    data = checkIn
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse(success = false, message = e.message ?: "Invalid request"))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse(success = false, message = e.message ?: "Conflict with current state"))
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(success = false, message = "Access denied"))
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

    /** Check out a child from a service */
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    fun checkOutChild(
        @Valid @RequestBody request: KidsCheckOutRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<KidsCheckOutResponse>> {
        return try {
            val userId = authentication.getCurrentUserId()
            val checkOut = kidsService.checkOutChild(userId, request)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Child checked out successfully",
                    data = checkOut
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse(success = false, message = e.message ?: "Invalid request"))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse(success = false, message = e.message ?: "Conflict with current state"))
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(success = false, message = "Access denied"))
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

    /** Get current check-ins */
    @GetMapping("/checkins/current")
    @PreAuthorize("hasRole('USER')")
    fun getCurrentCheckIns(
        @RequestParam(required = false) serviceId: Long?
    ): ResponseEntity<ApiResponse<List<KidsCheckInResponse>>> {
        return try {
            val checkIns = kidsService.getCurrentCheckIns(serviceId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Current check-ins retrieved successfully",
                    data = checkIns
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while retrieving current check-ins"
                    )
                )
        }
    }

    /** Get check-in history */
    @GetMapping("/checkins/history")
    @PreAuthorize("hasRole('USER')")
    fun getCheckInHistory(
        @RequestParam(required = false) childId: Long?,
        @RequestParam(required = false) serviceId: Long?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<List<KidsCheckInResponse>>> {
        return try {
            val userId = authentication.getCurrentUserId()
            val history = kidsService.getCheckInHistory(userId, childId, serviceId, page, pageSize)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Check-in history retrieved successfully",
                    data = history
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while retrieving check-in history"
                    )
                )
        }
    }
}