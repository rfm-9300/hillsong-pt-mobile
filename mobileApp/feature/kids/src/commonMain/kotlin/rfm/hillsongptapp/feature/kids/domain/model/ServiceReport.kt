package rfm.hillsongptapp.feature.kids.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a service report for staff/reporting features
 */
@Serializable
data class ServiceReport(
    val serviceId: String,
    val serviceName: String,
    val totalCapacity: Int,
    val currentCheckIns: Int,
    val availableSpots: Int,
    val checkedInChildren: List<Child>,
    val staffMembers: List<String>,
    val generatedAt: String // ISO 8601 format
) {
    init {
        require(serviceId.isNotBlank()) { "Service ID cannot be blank" }
        require(serviceName.isNotBlank()) { "Service name cannot be blank" }
        require(totalCapacity > 0) { "Total capacity must be positive" }
        require(currentCheckIns >= 0) { "Current check-ins cannot be negative" }
        require(currentCheckIns <= totalCapacity) { "Current check-ins cannot exceed total capacity" }
        require(availableSpots >= 0) { "Available spots cannot be negative" }
        require(availableSpots == totalCapacity - currentCheckIns) { "Available spots must equal total capacity minus current check-ins" }
        require(checkedInChildren.size == currentCheckIns) { "Number of checked-in children must match current check-ins count" }
        require(generatedAt.isNotBlank()) { "Generated at timestamp cannot be blank" }
    }
    
    /**
     * Check if the service is at full capacity
     */
    fun isAtCapacity(): Boolean = currentCheckIns >= totalCapacity
    
    /**
     * Get the capacity utilization as a percentage (0.0 to 1.0)
     */
    fun getCapacityUtilization(): Float = currentCheckIns.toFloat() / totalCapacity.toFloat()
    
    /**
     * Get the capacity utilization as a percentage (0 to 100)
     */
    fun getCapacityUtilizationPercent(): Int = (getCapacityUtilization() * 100).toInt()
    
    /**
     * Check if the service has available spots
     */
    fun hasAvailableSpots(): Boolean = availableSpots > 0
    
    /**
     * Get capacity information as a formatted string
     */
    fun getCapacityDisplay(): String = "$currentCheckIns/$totalCapacity"
    
    /**
     * Get a summary of the service status
     */
    fun getStatusSummary(): String = when {
        isAtCapacity() -> "Full"
        availableSpots <= 3 -> "Nearly Full"
        currentCheckIns == 0 -> "Empty"
        else -> "Available"
    }
}