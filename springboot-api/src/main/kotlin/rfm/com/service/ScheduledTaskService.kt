package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * Service for running scheduled tasks.
 * Note: Password reset and token cleanup are now handled by the auth-service.
 */
@Service
class ScheduledTaskService {
    
    private val logger = LoggerFactory.getLogger(ScheduledTaskService::class.java)
    
    /**
     * Placeholder for scheduled tasks.
     * Password reset token cleanup has been moved to auth-service.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    fun runScheduledMaintenance() {
        logger.debug("Running scheduled maintenance tasks")
        // Future scheduled tasks can be added here
    }
}