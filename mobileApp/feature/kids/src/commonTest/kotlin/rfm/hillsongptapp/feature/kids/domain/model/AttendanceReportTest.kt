package rfm.hillsongptapp.feature.kids.domain.model

import kotlin.test.*

/**
 * Unit tests for AttendanceReport domain model
 * Tests calculations, validations, and utility methods
 */
class AttendanceReportTest {
    
    @Test
    fun `attendance report should calculate average check-ins per day correctly`() {
        // Given
        val report = createTestAttendanceReport(
            totalCheckIns = 30,
            dailyBreakdown = mapOf(
                "2024-01-01" to 10,
                "2024-01-02" to 12,
                "2024-01-03" to 8
            )
        )
        
        // When
        val average = report.getAverageCheckInsPerDay()
        
        // Then
        assertEquals(10.0, average) // 30 total / 3 days = 10.0
    }
    
    @Test
    fun `attendance report should handle empty daily breakdown`() {
        // Given
        val report = createTestAttendanceReport(
            totalCheckIns = 0,
            dailyBreakdown = emptyMap()
        )
        
        // When
        val average = report.getAverageCheckInsPerDay()
        
        // Then
        assertEquals(0.0, average)
    }
    
    @Test
    fun `attendance report should find most popular service`() {
        // Given
        val report = createTestAttendanceReport(
            serviceBreakdown = mapOf(
                "toddlers" to 15,
                "kids" to 25,
                "youth" to 10
            )
        )
        
        // When
        val mostPopular = report.getMostPopularService()
        
        // Then
        assertNotNull(mostPopular)
        assertEquals("kids", mostPopular.first)
        assertEquals(25, mostPopular.second)
    }
    
    @Test
    fun `attendance report should handle empty service breakdown`() {
        // Given
        val report = createTestAttendanceReport(
            serviceBreakdown = emptyMap()
        )
        
        // When
        val mostPopular = report.getMostPopularService()
        
        // Then
        assertNull(mostPopular)
    }
    
    @Test
    fun `attendance report should find busiest day`() {
        // Given
        val report = createTestAttendanceReport(
            dailyBreakdown = mapOf(
                "2024-01-01" to 12,
                "2024-01-02" to 18,
                "2024-01-03" to 8
            )
        )
        
        // When
        val busiestDay = report.getBusiestDay()
        
        // Then
        assertNotNull(busiestDay)
        assertEquals("2024-01-02", busiestDay.first)
        assertEquals(18, busiestDay.second)
    }
    
    @Test
    fun `attendance report should calculate average check-ins per child`() {
        // Given
        val report = createTestAttendanceReport(
            totalCheckIns = 45,
            uniqueChildren = 15
        )
        
        // When
        val average = report.getAverageCheckInsPerChild()
        
        // Then
        assertEquals(3.0, average) // 45 total / 15 children = 3.0
    }
    
    @Test
    fun `attendance report should handle zero unique children`() {
        // Given
        val report = createTestAttendanceReport(
            totalCheckIns = 0,
            uniqueChildren = 0
        )
        
        // When
        val average = report.getAverageCheckInsPerChild()
        
        // Then
        assertEquals(0.0, average)
    }
    
    @Test
    fun `attendance report should count days covered`() {
        // Given
        val report = createTestAttendanceReport(
            dailyBreakdown = mapOf(
                "2024-01-01" to 10,
                "2024-01-02" to 12,
                "2024-01-03" to 8,
                "2024-01-04" to 15
            )
        )
        
        // When
        val daysCovered = report.getDaysCovered()
        
        // Then
        assertEquals(4, daysCovered)
    }
    
    @Test
    fun `attendance report should count services covered`() {
        // Given
        val report = createTestAttendanceReport(
            serviceBreakdown = mapOf(
                "toddlers" to 15,
                "kids" to 25,
                "youth" to 10
            )
        )
        
        // When
        val servicesCovered = report.getServicesCovered()
        
        // Then
        assertEquals(3, servicesCovered)
    }
    
    @Test
    fun `attendance report should identify single day report`() {
        // Given
        val report = createTestAttendanceReport(
            startDate = "2024-01-01",
            endDate = "2024-01-01"
        )
        
        // Then
        assertTrue(report.isSingleDay())
    }
    
    @Test
    fun `attendance report should identify multi-day report`() {
        // Given
        val report = createTestAttendanceReport(
            startDate = "2024-01-01",
            endDate = "2024-01-07"
        )
        
        // Then
        assertFalse(report.isSingleDay())
    }
    
    @Test
    fun `attendance report should generate correct summary for single day`() {
        // Given
        val report = createTestAttendanceReport(
            startDate = "2024-01-01",
            endDate = "2024-01-01",
            totalCheckIns = 25,
            uniqueChildren = 15,
            serviceBreakdown = mapOf("toddlers" to 10, "kids" to 15),
            dailyBreakdown = mapOf("2024-01-01" to 25)
        )
        
        // When
        val summary = report.getSummary()
        
        // Then
        assertEquals("25 total check-ins by 15 unique children across 2 services", summary)
    }
    
    @Test
    fun `attendance report should generate correct summary for multi-day`() {
        // Given
        val report = createTestAttendanceReport(
            startDate = "2024-01-01",
            endDate = "2024-01-03",
            totalCheckIns = 45,
            uniqueChildren = 20,
            serviceBreakdown = mapOf("toddlers" to 20, "kids" to 25),
            dailyBreakdown = mapOf(
                "2024-01-01" to 15,
                "2024-01-02" to 18,
                "2024-01-03" to 12
            )
        )
        
        // When
        val summary = report.getSummary()
        
        // Then
        assertEquals("45 total check-ins by 20 unique children over 3 days across 2 services", summary)
    }
    
    @Test
    fun `attendance report should validate required fields`() {
        // Test blank start date
        assertFailsWith<IllegalArgumentException> {
            AttendanceReport(
                startDate = "",
                endDate = "2024-01-07",
                totalCheckIns = 10,
                uniqueChildren = 5,
                serviceBreakdown = emptyMap(),
                dailyBreakdown = emptyMap(),
                generatedAt = "2024-01-01T12:00:00Z"
            )
        }
        
        // Test blank end date
        assertFailsWith<IllegalArgumentException> {
            AttendanceReport(
                startDate = "2024-01-01",
                endDate = "",
                totalCheckIns = 10,
                uniqueChildren = 5,
                serviceBreakdown = emptyMap(),
                dailyBreakdown = emptyMap(),
                generatedAt = "2024-01-01T12:00:00Z"
            )
        }
        
        // Test blank generated at
        assertFailsWith<IllegalArgumentException> {
            AttendanceReport(
                startDate = "2024-01-01",
                endDate = "2024-01-07",
                totalCheckIns = 10,
                uniqueChildren = 5,
                serviceBreakdown = emptyMap(),
                dailyBreakdown = emptyMap(),
                generatedAt = ""
            )
        }
    }
    
    @Test
    fun `attendance report should validate negative values`() {
        // Test negative total check-ins
        assertFailsWith<IllegalArgumentException> {
            AttendanceReport(
                startDate = "2024-01-01",
                endDate = "2024-01-07",
                totalCheckIns = -1,
                uniqueChildren = 5,
                serviceBreakdown = emptyMap(),
                dailyBreakdown = emptyMap(),
                generatedAt = "2024-01-01T12:00:00Z"
            )
        }
        
        // Test negative unique children
        assertFailsWith<IllegalArgumentException> {
            AttendanceReport(
                startDate = "2024-01-01",
                endDate = "2024-01-07",
                totalCheckIns = 10,
                uniqueChildren = -1,
                serviceBreakdown = emptyMap(),
                dailyBreakdown = emptyMap(),
                generatedAt = "2024-01-01T12:00:00Z"
            )
        }
    }
    
    @Test
    fun `attendance report should validate unique children not exceeding total`() {
        assertFailsWith<IllegalArgumentException> {
            AttendanceReport(
                startDate = "2024-01-01",
                endDate = "2024-01-07",
                totalCheckIns = 10,
                uniqueChildren = 15, // More unique children than total check-ins
                serviceBreakdown = emptyMap(),
                dailyBreakdown = emptyMap(),
                generatedAt = "2024-01-01T12:00:00Z"
            )
        }
    }
    
    // Helper function for creating test data
    private fun createTestAttendanceReport(
        startDate: String = "2024-01-01",
        endDate: String = "2024-01-07",
        totalCheckIns: Int = 25,
        uniqueChildren: Int = 15,
        serviceBreakdown: Map<String, Int> = mapOf("toddlers" to 10, "kids" to 15),
        dailyBreakdown: Map<String, Int> = mapOf("2024-01-01" to 12, "2024-01-02" to 13)
    ): AttendanceReport {
        return AttendanceReport(
            startDate = startDate,
            endDate = endDate,
            totalCheckIns = totalCheckIns,
            uniqueChildren = uniqueChildren,
            serviceBreakdown = serviceBreakdown,
            dailyBreakdown = dailyBreakdown,
            generatedAt = "2024-01-01T12:00:00Z"
        )
    }
}