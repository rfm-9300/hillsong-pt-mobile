package rfm.hillsongptapp.feature.kids.domain.validation

import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.KidsService

/**
 * Validator for business rules in kids management operations
 */
class BusinessRuleValidator {
    
    /**
     * Validate age requirements for service eligibility
     */
    fun validateAgeRequirements(child: Child, service: KidsService): ValidationResult {
        val childAge = child.calculateAge()
        
        return when {
            childAge < service.minAge -> ValidationResult.Invalid(
                "Child must be at least ${service.minAge} years old for ${service.name}. " +
                "Current age: $childAge years."
            )
            childAge > service.maxAge -> ValidationResult.Invalid(
                "Child must be under ${service.maxAge} years old for ${service.name}. " +
                "Current age: $childAge years."
            )
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate service capacity limits
     */
    fun validateServiceCapacity(service: KidsService): ValidationResult {
        return when {
            service.currentCapacity >= service.maxCapacity -> ValidationResult.Invalid(
                "${service.name} is at full capacity (${service.currentCapacity}/${service.maxCapacity}). " +
                "Please try another service or wait for availability."
            )
            service.currentCapacity < 0 -> ValidationResult.Invalid(
                "Invalid capacity data for ${service.name}. Please refresh and try again."
            )
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate service availability for check-ins
     */
    fun validateServiceAvailability(service: KidsService): ValidationResult {
        return when {
            !service.isAcceptingCheckIns -> ValidationResult.Invalid(
                "${service.name} is not currently accepting check-ins. " +
                "Please contact staff or try another service."
            )
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate child's current status for check-in
     */
    fun validateChildStatusForCheckIn(child: Child): ValidationResult {
        return when (child.status) {
            CheckInStatus.CHECKED_IN -> ValidationResult.Invalid(
                "${child.name} is already checked into ${child.currentServiceId ?: "a service"}. " +
                "Please check them out first before checking into another service."
            )
            CheckInStatus.CHECKED_OUT, CheckInStatus.NOT_IN_SERVICE -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate child's current status for check-out
     */
    fun validateChildStatusForCheckOut(child: Child): ValidationResult {
        return when (child.status) {
            CheckInStatus.CHECKED_OUT -> ValidationResult.Invalid(
                "${child.name} is already checked out. No action needed."
            )
            CheckInStatus.NOT_IN_SERVICE -> ValidationResult.Invalid(
                "${child.name} is not currently in any service. Cannot check out."
            )
            CheckInStatus.CHECKED_IN -> {
                if (child.currentServiceId.isNullOrBlank()) {
                    ValidationResult.Invalid(
                        "Invalid check-in data for ${child.name}. Please refresh and try again."
                    )
                } else {
                    ValidationResult.Valid
                }
            }
        }
    }
    
    /**
     * Validate complete check-in eligibility
     */
    fun validateCheckInEligibility(child: Child, service: KidsService): FormValidationResult {
        return FormValidationResult.from(
            "childStatus" to validateChildStatusForCheckIn(child),
            "serviceAvailability" to validateServiceAvailability(service),
            "serviceCapacity" to validateServiceCapacity(service),
            "ageRequirements" to validateAgeRequirements(child, service)
        )
    }
    
    /**
     * Validate complete check-out eligibility
     */
    fun validateCheckOutEligibility(child: Child): FormValidationResult {
        return FormValidationResult.from(
            "childStatus" to validateChildStatusForCheckOut(child)
        )
    }
    
    /**
     * Validate service time constraints
     */
    fun validateServiceTiming(service: KidsService): ValidationResult {
        // This would typically check against current time
        // For now, we'll validate that the service has valid time data
        return when {
            service.startTime.isBlank() || service.endTime.isBlank() -> ValidationResult.Invalid(
                "Service timing information is incomplete for ${service.name}"
            )
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate concurrent check-in attempts
     */
    fun validateConcurrentCheckIn(
        child: Child,
        service: KidsService,
        currentCapacity: Int
    ): ValidationResult {
        // Check if capacity has changed since last check
        return when {
            currentCapacity != service.currentCapacity -> ValidationResult.Invalid(
                "Service capacity has changed. Please refresh and try again."
            )
            currentCapacity >= service.maxCapacity -> ValidationResult.Invalid(
                "${service.name} became full while processing your request. Please try another service."
            )
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate parent/guardian authorization
     */
    fun validateParentAuthorization(child: Child, parentId: String): ValidationResult {
        return when {
            child.parentId != parentId -> ValidationResult.Invalid(
                "You are not authorized to manage this child's check-in status."
            )
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate service staff requirements
     */
    fun validateServiceStaffing(service: KidsService): ValidationResult {
        return when {
            service.staffMembers.isEmpty() -> ValidationResult.Invalid(
                "${service.name} currently has no assigned staff. Please contact administration."
            )
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Get comprehensive business rule validation for check-in
     */
    fun getComprehensiveCheckInValidation(
        child: Child,
        service: KidsService,
        parentId: String
    ): FormValidationResult {
        return FormValidationResult.from(
            "parentAuthorization" to validateParentAuthorization(child, parentId),
            "childStatus" to validateChildStatusForCheckIn(child),
            "serviceAvailability" to validateServiceAvailability(service),
            "serviceCapacity" to validateServiceCapacity(service),
            "ageRequirements" to validateAgeRequirements(child, service),
            "serviceTiming" to validateServiceTiming(service),
            "serviceStaffing" to validateServiceStaffing(service)
        )
    }
    
    /**
     * Get comprehensive business rule validation for check-out
     */
    fun getComprehensiveCheckOutValidation(
        child: Child,
        parentId: String
    ): FormValidationResult {
        return FormValidationResult.from(
            "parentAuthorization" to validateParentAuthorization(child, parentId),
            "childStatus" to validateChildStatusForCheckOut(child)
        )
    }
    
    /**
     * Get user-friendly business rule summary
     */
    fun getBusinessRuleSummary(validationResult: FormValidationResult): String {
        return if (validationResult.isValid) {
            "All business rules satisfied"
        } else {
            val primaryError = validationResult.allErrorMessages.firstOrNull()
            primaryError ?: "Business rule validation failed"
        }
    }
}