package rfm.hillsongptapp.feature.kids.domain.usecase

import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import co.touchlab.kermit.Logger

/**
 * Use case for checking out a child from their current service with comprehensive validation
 */
class CheckOutChildUseCase(
    private val kidsRepository: KidsRepository
) {
    private val logger = Logger.withTag("CheckOutChildUseCase")
    
    /**
     * Check out a child from their current service with full validation
     * 
     * @param childId The ID of the child to check out
     * @param checkedOutBy The ID of the user performing the check-out
     * @param notes Optional notes for the check-out
     * @return Result containing the updated check-in record or error
     */
    suspend fun execute(
        childId: String,
        checkedOutBy: String,
        notes: String? = null
    ): Result<Any> {
        return try {
            logger.d { "Starting check-out process for child $childId" }
            
            // Step 1: Validate child exists and get current status
            val childResult = kidsRepository.getChildById(childId)
            if (childResult.isFailure) {
                logger.w { "Child not found: $childId" }
                return Result.failure(KidsManagementError.ChildNotFound)
            }
            
            val child = childResult.getOrThrow()
            logger.d { "Found child: ${child.name}, current status: ${child.status}" }
            
            // Step 2: Validate child is currently checked in
            val availabilityValidation = validateChildCheckedIn(child)
            if (availabilityValidation.isFailure) {
                return availabilityValidation
            }
            
            // Step 3: Get the current service information
            val currentService = if (child.currentServiceId != null) {
                val serviceResult = kidsRepository.getServiceById(child.currentServiceId)
                if (serviceResult.isFailure) {
                    logger.w { "Current service not found: ${child.currentServiceId}" }
                    null // Continue with check-out even if service not found
                } else {
                    serviceResult.getOrThrow()
                }
            } else {
                null
            }
            
            if (currentService != null) {
                logger.d { "Child is currently in service: ${currentService.name}" }
            } else {
                logger.w { "Child appears to be checked in but current service not found" }
            }
            
            // Step 4: Perform the check-out
            logger.d { "All validations passed, performing check-out" }
            val checkOutResult = kidsRepository.checkOutChild(
                childId = childId,
                checkedOutBy = checkedOutBy,
                notes = notes
            )
            
            if (checkOutResult.isSuccess) {
                val record = checkOutResult.getOrThrow()
                logger.i { "Successfully checked out child ${child.name} from service at ${record.checkOutTime}" }
                
                // Return comprehensive result with service information
                val result = CheckOutResult(
                    record = record,
                    child = child,
                    service = currentService
                )
                Result.success(result)
            } else {
                logger.e { "Check-out failed: ${checkOutResult.exceptionOrNull()?.message}" }
                checkOutResult.map { record ->
                    CheckOutResult(
                        record = record,
                        child = child,
                        service = currentService
                    )
                }
            }
            
        } catch (e: Exception) {
            logger.e(e) { "Unexpected error during check-out process" }
            Result.failure(KidsManagementError.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Validate that the child is currently checked in and can be checked out
     */
    private fun validateChildCheckedIn(child: Child): Result<Unit> {
        return when (child.status) {
            CheckInStatus.CHECKED_IN -> {
                logger.d { "Child ${child.name} is checked in and can be checked out" }
                Result.success(Unit)
            }
            CheckInStatus.CHECKED_OUT -> {
                logger.w { "Child ${child.name} is already checked out" }
                Result.failure(KidsManagementError.ChildNotCheckedIn)
            }
            CheckInStatus.NOT_IN_SERVICE -> {
                logger.w { "Child ${child.name} is not in any service" }
                Result.failure(KidsManagementError.ChildNotCheckedIn)
            }
        }
    }
    
    /**
     * Get check-out eligibility information for a child
     */
    suspend fun getCheckOutEligibilityInfo(childId: String): Result<CheckOutEligibilityInfo> {
        return try {
            val childResult = kidsRepository.getChildById(childId)
            if (childResult.isFailure) {
                return Result.failure(KidsManagementError.ChildNotFound)
            }
            
            val child = childResult.getOrThrow()
            val canCheckOut = child.status.canBeCheckedOut()
            
            val currentService = if (child.currentServiceId != null && canCheckOut) {
                val serviceResult = kidsRepository.getServiceById(child.currentServiceId)
                serviceResult.getOrNull()
            } else {
                null
            }
            
            val eligibilityInfo = CheckOutEligibilityInfo(
                child = child,
                canCheckOut = canCheckOut,
                currentService = currentService,
                checkInTime = child.checkInTime,
                reason = if (!canCheckOut) {
                    when (child.status) {
                        CheckInStatus.CHECKED_OUT -> "Child is already checked out"
                        CheckInStatus.NOT_IN_SERVICE -> "Child is not currently in any service"
                        else -> "Unknown reason"
                    }
                } else null
            )
            
            Result.success(eligibilityInfo)
        } catch (e: Exception) {
            logger.e(e) { "Error getting check-out eligibility for child $childId" }
            Result.failure(KidsManagementError.UnknownError(e.message ?: "Unknown error"))
        }
    }
}

/**
 * Result of a successful check-out operation including all relevant information
 */
data class CheckOutResult(
    val record: CheckInRecord,
    val child: Child,
    val service: KidsService?
) {
    /**
     * Get a summary of the check-out for display purposes
     */
    fun getSummary(): String {
        val serviceName = service?.name ?: "Unknown Service"
        val checkInTime = formatTime(record.checkInTime)
        val checkOutTime = record.checkOutTime?.let { formatTime(it) } ?: "Now"
        
        return "Checked out ${child.name} from $serviceName. " +
                "Checked in at $checkInTime, checked out at $checkOutTime."
    }
    
    /**
     * Get the duration of the service attendance
     */
    fun getAttendanceDuration(): String? {
        return if (record.checkOutTime != null) {
            // This is a simplified duration calculation
            // In a real implementation, you'd use proper date/time libraries
            try {
                val checkInTime = record.checkInTime
                val checkOutTime = record.checkOutTime
                // For now, just return a placeholder
                "Duration calculated" // TODO: Implement proper duration calculation
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    private fun formatTime(isoTime: String): String {
        // Simplified time formatting - extract time portion from ISO string
        return try {
            val timePart = isoTime.substringAfter('T').substringBefore('.')
            val (hour, minute) = timePart.split(':')
            val hourInt = hour.toInt()
            val amPm = if (hourInt >= 12) "PM" else "AM"
            val displayHour = if (hourInt == 0) 12 else if (hourInt > 12) hourInt - 12 else hourInt
            "$displayHour:$minute $amPm"
        } catch (e: Exception) {
            isoTime // Fallback to original string
        }
    }
}

/**
 * Information about a child's check-out eligibility
 */
data class CheckOutEligibilityInfo(
    val child: Child,
    val canCheckOut: Boolean,
    val currentService: KidsService?,
    val checkInTime: String?,
    val reason: String? = null
)