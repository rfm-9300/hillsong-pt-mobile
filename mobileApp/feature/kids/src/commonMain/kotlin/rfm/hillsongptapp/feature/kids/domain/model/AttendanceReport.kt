package rfm.hillsongptapp.feature.kids.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing an attendance report for a date range
 */
@Serializable
data class AttendanceReport(
    val startDate: String, // ISO 8601 format
    val endDate: String, // ISO 8601 format
    val totalCheckIns: Int,
    val uniqueChildren: Int,
    val serviceBreakdown: Map<String, Int>, // Service ID to check-in count
    val dailyBreakdown: Map<String, Int>, // Date to check-in count
    val generatedAt: String // ISO 8601 format
) {
    init {
        require(startDate.isNotBlank()) { "Start date cannot be blank" }
        require(endDate.isNotBlank()) { "End date cannot be blank" }
        require(totalCheckIns >= 0) { "Total check-ins cannot be negative" }
        require(uniqueChildren >= 0) { "Unique children count cannot be negative" }
        require(uniqueChildren <= totalCheckIns) { "Unique children cannot exceed total check-ins" }
        require(generatedAt.isNotBlank()) { "Generated at timestamp cannot be blank" }
    }
    
    /**
     * Get the average check-ins per day
     */
    fun getAverageCheckInsPerDay(): Double {
        return if (dailyBreakdown.isEmpty()) {
            0.0
        } else {
            totalCheckIns.toDouble() / dailyBreakdown.size
        }
    }
    
    /**
     * Get the most popular service (by check-in count)
     */
    fun getMostPopularService(): Pair<String, Int>? {
        return serviceBreakdown.maxByOrNull { it.value }?.toPair()
    }
    
    /**
     * Get the busiest day (by check-in count)
     */
    fun getBusiestDay(): Pair<String, Int>? {
        return dailyBreakdown.maxByOrNull { it.value }?.toPair()
    }
    
    /**
     * Get the average check-ins per child
     */
    fun getAverageCheckInsPerChild(): Double {
        return if (uniqueChildren == 0) {
            0.0
        } else {
            totalCheckIns.toDouble() / uniqueChildren
        }
    }
    
    /**
     * Get the number of days covered by this report
     */
    fun getDaysCovered(): Int = dailyBreakdown.size
    
    /**
     * Get the number of services covered by this report
     */
    fun getServicesCovered(): Int = serviceBreakdown.size
    
    /**
     * Check if this is a single-day report
     */
    fun isSingleDay(): Boolean = startDate == endDate
    
    /**
     * Get a summary of the report
     */
    fun getSummary(): String {
        return buildString {
            append("$totalCheckIns total check-ins")
            append(" by $uniqueChildren unique children")
            if (!isSingleDay()) {
                append(" over ${getDaysCovered()} days")
            }
            append(" across ${getServicesCovered()} services")
        }
    }
}