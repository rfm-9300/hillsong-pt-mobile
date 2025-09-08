package rfm.hillsongptapp.feature.kids.domain.usecase

import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import co.touchlab.kermit.Logger

/**
 * Use case for checking in a child to a service with comprehensive validation
 */
class CheckInChildUseCase(
    private val kidsRepository: KidsRepository
) {
    private val logger = Logger.withTag("CheckInChildUseCase")
    
    /**
     * Check in a child to a service with full validation
     * 
     * @param childId The ID of the child to check in
     * @param serviceId The ID of the service to check into
     * @param checkedInBy The ID of the user performing the check-in
     * @param notes Optional notes for the check-in
     * @return Result containing the check-in record or error
     */
    suspend fun execute(
        childId: String,
        serviceId: String,
        checkedInBy: String,
        notes: String? = null
    ): Result<Any> {
        return try {
            logger.d { "Starting check-in process for child $childId to service $serviceId" }
            
            // Step 1: Validate child exists and get current status
            val childResult = kidsRepository.getChildById(childId)
            if (childResult.isFailure) {
                logger.w { "Child not found: $childId" }
                return Result.failure(KidsManagementError.ChildNotFound)
            }
            
            val child = childResult.getOrThrow()
            logger.d { "Found child: ${child.name}, current status: ${child.status}" }
            
            // Step 2: Validate child is available for check-in
            val availabilityValidation = validateChildAvailability(child)
            if (availabilityValidation.isFailure) {
                return availabilityValidation
            }
            
            // Step 3: Validate service exists and get current capacity
            val serviceResult = kidsRepository.getServiceById(serviceId)
            if (serviceResult.isFailure) {
                logger.w { "Service not found: $serviceId" }
                return Result.failure(KidsManagementError.ServiceNotFound)
            }
            
            val service = serviceResult.getOrThrow()
            logger.d { "Found service: ${service.name}, capacity: ${service.currentCapacity}/${service.maxCapacity}" }
            
            // Step 4: Validate service can accept check-ins
            val serviceValidation = validateServiceAvailability(service)
            if (serviceValidation.isFailure) {
                return serviceValidation
            }
            
            // Step 5: Validate age eligibility
            val ageValidation = validateAgeEligibility(child, service)
            if (ageValidation.isFailure) {
                return ageValidation
            }
            
            // Step 6: Validate capacity
            val capacityValidation = validateServiceCapacity(service)
            if (capacityValidation.isFailure) {
                return capacityValidation
            }
            
            // Step 7: Perform the check-in
            logger.d { "All validations passed, performing check-in" }
            val checkInResult = kidsRepository.checkInChild(
                childId = childId,
                serviceId = serviceId,
                checkedInBy = checkedInBy,
                notes = notes
            )
            
            if (checkInResult.isSuccess) {
                val record = checkInResult.getOrThrow()
                logger.i { "Successfully checked in child ${child.name} to service ${service.name} at ${record.checkInTime}" }
            } else {
                logger.e { "Check-in failed: ${checkInResult.exceptionOrNull()?.message}" }
            }
            
            checkInResult
            
        } catch (e: Exception) {
            logger.e(e) { "Unexpected error during check-in process" }
            Result.failure(KidsManagementError.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Validate that the child is available for check-in
     */
    private fun validateChildAvailability(child: Child): Result<Unit> {
        return when (child.status) {
            CheckInStatus.CHECKED_IN -> {
                logger.w { "Child ${child.name} is already checked in to service ${child.currentServiceId}" }
                Result.failure(KidsManagementError.ChildAlreadyCheckedIn)
            }
            CheckInStatus.CHECKED_OUT, CheckInStatus.NOT_IN_SERVICE -> {
                logger.d { "Child ${child.name} is available for check-in" }
                Result.success(Unit)
            }
        }
    }
    
    /**
     * Validate that the service can accept check-ins
     */
    private fun validateServiceAvailability(service: KidsService): Result<Unit> {
        return if (!service.isAcceptingCheckIns) {
            logger.w { "Service ${service.name} is not currently accepting check-ins" }
            Result.failure(KidsManagementError.ServiceNotAcceptingCheckIns)
        } else {
            logger.d { "Service ${service.name} is accepting check-ins" }
            Result.success(Unit)
        }
    }
    
    /**
     * Validate that the child meets age requirements for the service
     */
    private fun validateAgeEligibility(child: Child, service: KidsService): Result<Unit> {
        val childAge = child.calculateAge()
        return if (!service.isAgeEligible(childAge)) {
            logger.w { 
                "Child ${child.name} (age $childAge) is not eligible for service ${service.name} " +
                "(age range: ${service.minAge}-${service.maxAge})"
            }
            Result.failure(KidsManagementError.InvalidAgeForService)
        } else {
            logger.d { "Child ${child.name} (age $childAge) is eligible for service ${service.name}" }
            Result.success(Unit)
        }
    }
    
    /**
     * Validate that the service has available capacity
     */
    private fun validateServiceCapacity(service: KidsService): Result<Unit> {
        return if (service.isAtCapacity()) {
            logger.w { 
                "Service ${service.name} is at full capacity " +
                "(${service.currentCapacity}/${service.maxCapacity})"
            }
            Result.failure(KidsManagementError.ServiceAtCapacity)
        } else {
            logger.d { 
                "Service ${service.name} has available capacity " +
                "(${service.getAvailableSpots()} spots remaining)"
            }
            Result.success(Unit)
        }
    }
    
    /**
     * Get eligible services for a child with detailed validation information
     */
    suspend fun getEligibleServicesForChild(childId: String): Result<CheckInEligibilityInfo> {
        return try {
            val childResult = kidsRepository.getChildById(childId)
            if (childResult.isFailure) {
                return Result.failure(KidsManagementError.ChildNotFound)
            }
            
            val child = childResult.getOrThrow()
            val servicesResult = kidsRepository.getServicesAcceptingCheckIns()
            if (servicesResult.isFailure) {
                return Result.failure(servicesResult.exceptionOrNull() ?: Exception("Failed to get services"))
            }
            
            val services = servicesResult.getOrThrow()
            val eligibleServices = services.mapNotNull { service ->
                val ageEligible = service.isAgeEligible(child.calculateAge())
                val hasCapacity = service.hasAvailableSpots()
                val isAccepting = service.isAcceptingCheckIns
                
                if (ageEligible && hasCapacity && isAccepting) {
                    EligibleServiceInfo(
                        service = service,
                        availableSpots = service.getAvailableSpots(),
                        isRecommended = service.getAvailableSpots() > 5 // Recommend services with more spots
                    )
                } else null
            }
            
            val eligibilityInfo = CheckInEligibilityInfo(
                child = child,
                eligibleServices = eligibleServices
            )
            
            Result.success(eligibilityInfo)
        } catch (e: Exception) {
            logger.e(e) { "Error getting eligible services for child $childId" }
            Result.failure(KidsManagementError.UnknownError(e.message ?: "Unknown error"))
        }
    }
}

/**
 * Information about a service that is eligible for a child
 */
data class EligibleServiceInfo(
    val service: KidsService,
    val availableSpots: Int,
    val isRecommended: Boolean
)

/**
 * Complete check-in eligibility information including child and eligible services
 */
data class CheckInEligibilityInfo(
    val child: Child,
    val eligibleServices: List<EligibleServiceInfo>
)