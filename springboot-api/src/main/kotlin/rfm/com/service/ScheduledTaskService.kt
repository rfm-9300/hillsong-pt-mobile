package rfm.com.service

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import rfm.com.repository.UserRepository
import java.util.UUID

/**
 * Service for running scheduled tasks.
 * Note: Password reset and token cleanup are now handled by the auth-service.
 */
@Service
class ScheduledTaskService(private val userRepository: UserRepository) {

    private val logger = LoggerFactory.getLogger(ScheduledTaskService::class.java)

    @PostConstruct
    fun assignMissingQrTokens() {
        val users = userRepository.findAll().filter { it.qrToken == null }
        if (users.isEmpty()) return
        logger.info("Assigning qrToken to ${users.size} existing users")
        userRepository.saveAll(users.map { it.copy(qrToken = UUID.randomUUID().toString()) })
    }

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