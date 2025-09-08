package rfm.hillsongptapp.feature.kids.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import rfm.hillsongptapp.feature.kids.domain.model.AttendanceReport
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.model.ServiceReport

/**
 * ViewModel for the Reports screen
 * Manages state for attendance reports, service reports, and filtering
 */
class ReportsViewModel(
    private val kidsRepository: KidsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()
    
    init {
        // Set default date range to current week
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)
        
        _uiState.value = _uiState.value.copy(
            selectedStartDate = startOfWeek.toString(),
            selectedEndDate = endOfWeek.toString()
        )
    }
    
    /**
     * Load initial data including services and current reports
     */
    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load available services
                val servicesResult = kidsRepository.getAvailableServices()
                if (servicesResult.isSuccess) {
                    val services = servicesResult.getOrNull() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        availableServices = services,
                        selectedServices = services.map { it.id }.toSet()
                    )
                }
                
                // Load current service reports
                loadServiceReports()
                
                // Load attendance report for selected date range
                loadAttendanceReport()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error occurred",
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Refresh all data
     */
    fun refreshData() {
        loadInitialData()
    }
    
    /**
     * Update the selected date range and reload reports
     */
    fun updateDateRange(startDate: String, endDate: String) {
        _uiState.value = _uiState.value.copy(
            selectedStartDate = startDate,
            selectedEndDate = endDate
        )
        loadAttendanceReport()
    }
    
    /**
     * Update service filter selection
     */
    fun updateServiceFilter(selectedServices: Set<String>) {
        _uiState.value = _uiState.value.copy(selectedServices = selectedServices)
        loadAttendanceReport()
    }
    
    /**
     * Select a specific service for detailed view
     */
    fun selectService(serviceId: String) {
        _uiState.value = _uiState.value.copy(selectedServiceId = serviceId)
    }
    
    /**
     * Export the current attendance report
     */
    fun exportReport() {
        val report = _uiState.value.attendanceReport ?: return
        
        viewModelScope.launch {
            try {
                // In a real implementation, this would generate and save/share a file
                // For now, we'll just show a success message
                _uiState.value = _uiState.value.copy(
                    exportStatus = "Report exported successfully"
                )
                
                // Clear export status after a delay
                kotlinx.coroutines.delay(3000)
                _uiState.value = _uiState.value.copy(exportStatus = null)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to export report: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Load service reports for all services
     */
    private suspend fun loadServiceReports() {
        try {
            val serviceReports = mutableListOf<ServiceReport>()
            
            for (service in _uiState.value.availableServices) {
                val reportResult = kidsRepository.getServiceReport(service.id)
                if (reportResult.isSuccess) {
                    reportResult.getOrNull()?.let { report ->
                        serviceReports.add(report)
                    }
                }
            }
            
            _uiState.value = _uiState.value.copy(
                serviceReports = serviceReports.sortedByDescending { it.currentCheckIns }
            )
            
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to load service reports: ${e.message}"
            )
        }
    }
    
    /**
     * Load attendance report for the selected date range and services
     */
    private fun loadAttendanceReport() {
        viewModelScope.launch {
            try {
                val startDate = _uiState.value.selectedStartDate
                val endDate = _uiState.value.selectedEndDate
                
                if (startDate.isNotBlank() && endDate.isNotBlank()) {
                    val reportResult = kidsRepository.getAttendanceReport(startDate, endDate)
                    
                    if (reportResult.isSuccess) {
                        val report = reportResult.getOrNull()
                        
                        // Filter report by selected services if needed
                        val filteredReport = report?.let { originalReport ->
                            if (_uiState.value.selectedServices.size < _uiState.value.availableServices.size) {
                                // Filter the service breakdown to only include selected services
                                val filteredServiceBreakdown = originalReport.serviceBreakdown
                                    .filterKeys { serviceId -> _uiState.value.selectedServices.contains(serviceId) }
                                
                                val filteredTotalCheckIns = filteredServiceBreakdown.values.sum()
                                
                                originalReport.copy(
                                    totalCheckIns = filteredTotalCheckIns,
                                    serviceBreakdown = filteredServiceBreakdown
                                )
                            } else {
                                originalReport
                            }
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            attendanceReport = filteredReport,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load attendance report",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load attendance report: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}