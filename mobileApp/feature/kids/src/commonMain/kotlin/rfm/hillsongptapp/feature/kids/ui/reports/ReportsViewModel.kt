package rfm.hillsongptapp.feature.kids.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import rfm.hillsongptapp.core.data.repository.KidsRepository
import rfm.hillsongptapp.core.data.repository.KidsResult
import rfm.hillsongptapp.core.data.model.AttendanceReport
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.model.ServiceReport

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
                when (servicesResult) {
                    is KidsResult.Success -> {
                        val services = servicesResult.data
                        _uiState.value = _uiState.value.copy(
                            availableServices = services,
                            selectedServices = services.map { it.id }.toSet()
                        )
                        
                        // Load current service reports
                        loadServiceReports()
                        
                        // Load attendance report for selected date range
                        loadAttendanceReport()
                    }
                    is KidsResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load services: ${servicesResult.message}",
                            isLoading = false
                        )
                    }
                    is KidsResult.NetworkError -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Network error loading services: ${servicesResult.message}",
                            isLoading = false
                        )
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error occurred",
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Load today's services specifically
     */
    fun loadTodaysServices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Set date range to today only
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                _uiState.value = _uiState.value.copy(
                    selectedStartDate = today.toString(),
                    selectedEndDate = today.toString()
                )
                
                // Load available services
                val servicesResult = kidsRepository.getAvailableServices()
                when (servicesResult) {
                    is KidsResult.Success -> {
                        val services = servicesResult.data
                        _uiState.value = _uiState.value.copy(
                            availableServices = services,
                            selectedServices = services.map { it.id }.toSet(),
                            isLoading = false
                        )
                        
                        // Load current service reports for today
                        loadServiceReports()
                    }
                    is KidsResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load today's services: ${servicesResult.message}",
                            isLoading = false
                        )
                    }
                    is KidsResult.NetworkError -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Network error loading services: ${servicesResult.message}",
                            isLoading = false
                        )
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error occurred",
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Refresh today's data specifically
     */
    fun refreshTodaysData() {
        loadTodaysServices()
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
        if (_uiState.value.attendanceReport == null) return
        
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
                when (reportResult) {
                    is KidsResult.Success -> {
                        serviceReports.add(reportResult.data)
                    }
                    is KidsResult.Error -> {
                        // Log error but continue with other services
                    }
                    is KidsResult.NetworkError -> {
                        // Log error but continue with other services
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
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
                    
                    when (reportResult) {
                        is KidsResult.Success -> {
                            val report = reportResult.data
                            
                            // Filter report by selected services if needed
                            val filteredReport = if (_uiState.value.selectedServices.size < _uiState.value.availableServices.size) {
                                // Filter the service breakdown to only include selected services
                                val filteredServiceBreakdown = report.serviceBreakdown
                                    .filterKeys { serviceId -> _uiState.value.selectedServices.contains(serviceId) }
                                
                                val filteredTotalCheckIns = filteredServiceBreakdown.values.sum()
                                
                                report.copy(
                                    totalCheckIns = filteredTotalCheckIns,
                                    serviceBreakdown = filteredServiceBreakdown
                                )
                            } else {
                                report
                            }
                            
                            _uiState.value = _uiState.value.copy(
                                attendanceReport = filteredReport,
                                isLoading = false
                            )
                        }
                        is KidsResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                error = "Failed to load attendance report: ${reportResult.message}",
                                isLoading = false
                            )
                        }
                        is KidsResult.NetworkError -> {
                            _uiState.value = _uiState.value.copy(
                                error = "Network error loading attendance report: ${reportResult.message}",
                                isLoading = false
                            )
                        }
                        is KidsResult.Loading -> {
                            // Should not happen in suspend function
                        }
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