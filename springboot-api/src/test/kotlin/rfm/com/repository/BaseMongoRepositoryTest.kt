package rfm.com.repository

import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

/**
 * Base class for MongoDB repository integration tests. Boots a disposable
 * Mongo 6 container per class so queries (including 2dsphere geo ones)
 * run against a real engine.
 */
@DataMongoTest
@Testcontainers
abstract class BaseMongoRepositoryTest {

    companion object {
        @Container
        @JvmStatic
        val mongo: MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:6.0"))

        @DynamicPropertySource
        @JvmStatic
        fun mongoProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongo.replicaSetUrl }
            registry.add("spring.data.mongodb.auto-index-creation") { "true" }
        }
    }
}
