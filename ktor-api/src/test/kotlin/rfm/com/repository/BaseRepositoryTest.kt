package rfm.com.repository

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base class for repository integration tests using TestContainers
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class BaseRepositoryTest {
    
    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password")
        
        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.jpa.show-sql") { "true" }
            registry.add("spring.jpa.properties.hibernate.format_sql") { "true" }
        }
    }
}