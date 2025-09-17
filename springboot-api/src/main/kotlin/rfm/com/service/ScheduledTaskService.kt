package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduledTaskService(
    private val userService: UserService
) {
    
    private val logger = LoggerFactory.getLogger(ScheduledTaskService::class.java)
    
    /**
     * Clean up expired reset tokens every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    fun cleanupExpiredResetTokens() {
        logger.debug("Running scheduled cleanup of expired reset tokens")
        userService.cleanupExpiredResetTokens()
    }
}