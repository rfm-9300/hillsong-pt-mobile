package rfm.hillsongptapp.feature.kids.ui.reports

import rfm.hillsongptapp.feature.kids.domain.model.AttendanceReport
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.model.ServiceReport

/**
 * UI state for the Reports screen
 * Contains all data needed to display attendance reports and service management
 */
data class ReportsUiState(
    // Loading and error states
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Date range selection
    val selectedStartDate: String = "",
    val selectedEndDate: String = "",
    
    // Service filtering
    val availableServices: List<KidsService> = emptyList(),
    val selectedServices: Set<String> = emptySet(),
    val selectedServiceId: String? = null,
    
    // Report data
    val attendanceReport: AttendanceReport? = null,
    val serviceReports: List<ServiceReport> = emptyList(),
    
    // Export functionality
    val exportStatus: String? = null
) {
    /**
     * Check if any services are selected for filtering
     */
    fun hasServiceFilter(): Boolean = selectedServices.isNotEmpty() && selectedServices.size < availableServices.size
    
    /**
     * Get the names of selected services for display
     */
    fun getSelectedServiceNames(): List<String> {
        return availableServices
            .filter { selectedServices.contains(it.id) }
            .map { it.name }
    }
    
    /**
     * Check if a date range is selected
     */
    fun hasDateRange(): Boolean = selectedStartDate.isNotBlank() && selectedEndDate.isNotBlank()
    
    /**
     * Get formatted date range for display
     */
    fun getFormattedDateRange(): String {
        return if (hasDateRange()) {
            "$selectedStartDate to $selectedEndDate"
        } else {
            "No date range selected"
        }
    }
    
    /**
     * Check if there's any data to display
     */
    fun hasData(): Boolean = attendanceReport != null || serviceReports.isNotEmpty()
    
    /**
     * Get total capacity across all services
     */
    fun getTotalCapacity(): Int = serviceReports.sumOf { it.totalCapacity }
    
    /**
     * Get total current check-ins across all services
     */
    fun getTotalCurrentCheckIns(): Int = serviceReports.sumOf { it.currentCheckIns }
    
    /**
     * Get overall capacity utilization percentage
     */
    fun getOverallCapacityUtilization(): Int {
        val totalCapacity = getTotalCapacity()
        return if (totalCapacity > 0) {
            ((getTotalCurrentCheckIns().toFloat() / totalCapacity.toFloat()) * 100).toInt()
        } else {
            0
        }
    }
    
    /**
     * Get services that are at or near capacity (>= 90%)
     */
    fun getServicesNearCapacity(): List<ServiceReport> {
        return serviceReports.filter { it.getCapacityUtilization() >= 0.9f }
    }
    
    /**
     * Get services that are full
     */
    fun getFullServices(): List<ServiceReport> {
        return serviceReports.filter { it.isAtCapacity() }
    }
    
    /**
     * Check if any services are full
     */
    fun hasFullServices(): Boolean = getFullServices().isNotEmpty()
    
    /**
     * Check if any services are near capacity
     */
    fun hasServicesNearCapacity(): Boolean = getServicesNearCapacity().isNotEmpty()
}