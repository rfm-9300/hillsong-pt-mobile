package rfm.com

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
class ChurchManagementApplication

fun main(args: Array<String>) {
    runApplication<ChurchManagementApplication>(*args)
}
