package rfm.com.job

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import rfm.com.entity.KidsService
import rfm.com.repository.KidsServiceRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Service
class KidsServiceJob(
    private val kidsServiceRepository: KidsServiceRepository
) {

    private val logger = LoggerFactory.getLogger(KidsServiceJob::class.java)

    @Scheduled(cron = "0 30 8 * * SUN")
    fun createSundayServices() {
        logger.info("Creating Sunday kids services...")

        val servicesToCreate = listOf(
            "Morning 09h:30" to LocalTime.of(9, 30),
            "Morning 11h:30" to LocalTime.of(11, 30),
            "Evening 17h:30" to LocalTime.of(17, 30)
        )

        val today = LocalDate.now()
        servicesToCreate.forEach { (name, time) ->
            val service = KidsService(
                name = name,
                dayOfWeek = DayOfWeek.SUNDAY,
                serviceDate = today,
                startTime = time,
                endTime = time.plusHours(2),
                location = "Main Hall",
                maxCapacity = 50,
                minAge = 1,
                maxAge = 12,
                isActive = true,
                requiresPreRegistration = false
            )
            kidsServiceRepository.save(service)
            logger.info("Created service: {}", service.name)
        }

        logger.info("Finished creating Sunday kids services.")
    }
}