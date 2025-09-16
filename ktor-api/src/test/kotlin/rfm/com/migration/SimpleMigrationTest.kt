package rfm.com.migration

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class SimpleMigrationTest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("test_db")
            withUsername("test")
            withPassword("test")
        }
    }

    @Test
    fun `should run all migrations successfully`() {
        // Create Flyway instance
        val flyway = Flyway.configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .locations("classpath:db/migration")
            .load()

        // Clean and migrate
        flyway.clean()
        val migrationResult = flyway.migrate()

        // Verify migrations ran successfully
        assertTrue(migrationResult.migrationsExecuted > 0, "No migrations were executed")
        
        // Verify final state
        val info = flyway.info()
        val appliedMigrations = info.applied()
        
        assertTrue(appliedMigrations.isNotEmpty(), "No migrations were applied")
        
        // Check that we have the expected number of migrations
        val expectedMigrations = listOf("V1", "V2", "V3", "V4", "V5", "V6", "V7", "V8", "V9")
        val appliedVersions = appliedMigrations.map { it.version.version }
        
        expectedMigrations.forEach { expectedVersion ->
            assertTrue(
                appliedVersions.contains(expectedVersion),
                "Migration $expectedVersion was not applied. Applied: $appliedVersions"
            )
        }
        
        println("Successfully applied migrations: $appliedVersions")
    }

    @Test
    fun `should validate schema structure after migration`() {
        // Create Flyway instance and run migrations
        val flyway = Flyway.configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .locations("classpath:db/migration")
            .load()

        flyway.clean()
        flyway.migrate()

        // Test database connection and basic queries
        postgres.createConnection("").use { connection ->
            // Verify main tables exist
            val expectedTables = listOf(
                "users", "user_profile", "event", "post", "service", 
                "kids_service", "kid", "attendance", "password_reset", "user_token"
            )

            expectedTables.forEach { tableName ->
                val statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?"
                )
                statement.setString(1, tableName)
                val resultSet = statement.executeQuery()
                resultSet.next()
                val count = resultSet.getInt(1)
                assertTrue(count > 0, "Table $tableName does not exist")
                println("✓ Table $tableName exists")
            }

            // Verify key foreign key relationships exist
            val foreignKeyQuery = """
                SELECT COUNT(*) FROM information_schema.table_constraints 
                WHERE constraint_type = 'FOREIGN KEY' AND table_name IN (
                    'user_profile', 'event', 'post', 'attendance', 'kid', 'kids_service'
                )
            """.trimIndent()
            
            val fkStatement = connection.prepareStatement(foreignKeyQuery)
            val fkResultSet = fkStatement.executeQuery()
            fkResultSet.next()
            val fkCount = fkResultSet.getInt(1)
            assertTrue(fkCount > 0, "No foreign key constraints found")
            println("✓ Found $fkCount foreign key constraints")
            
            // Test basic insert to verify table structure
            val insertUserSql = """
                INSERT INTO users (email, password, salt, verified, auth_provider) 
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent()
            
            val insertStatement = connection.prepareStatement(insertUserSql)
            insertStatement.setString(1, "test@example.com")
            insertStatement.setString(2, "hashedpassword")
            insertStatement.setString(3, "salt123")
            insertStatement.setBoolean(4, true)
            insertStatement.setString(5, "LOCAL")
            
            val insertResult = insertStatement.executeUpdate()
            assertEquals(1, insertResult, "Failed to insert test user")
            println("✓ Successfully inserted test data")
        }
    }
}