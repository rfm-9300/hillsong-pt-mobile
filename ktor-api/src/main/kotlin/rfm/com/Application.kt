package rfm.com

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableScheduling
class ChurchManagementApplication

fun main(args: Array<String>) {
    runApplication<ChurchManagementApplication>(*args)
}
