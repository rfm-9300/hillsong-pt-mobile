package rfm.com.plugins

import rfm.com.data.db.event.EventAttendeeTable
import rfm.com.data.db.event.EventTable
import rfm.com.data.db.event.EventWaitingListTable
import rfm.com.data.db.image.ImageHashTable
import rfm.com.data.db.kidsservice.KidsCheckInTable
import rfm.com.data.db.kidsservice.KidsServiceTable
import rfm.com.data.db.post.PostCommentTable
import rfm.com.data.db.post.PostLikeTable
import rfm.com.data.db.post.PostTable
import rfm.com.data.db.service.ServiceTable
import rfm.com.data.db.user.UserTable
import rfm.com.data.db.user.UserProfilesTable
import rfm.com.data.db.user.PasswordResetTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases(config: ApplicationConfig) {
    val databaseName = config.property("storage.name").getString()
    val host = config.property("storage.host").getString()
    val driverClassName = "org.postgresql.Driver"
    val jdbcURL = "jdbc:postgresql://$host:5432/$databaseName"
    val dbUser = config.property("storage.user").getString()
    val dbPassword = config.property("storage.password").getString()



    Database.connect(url = jdbcURL, user = dbUser, password = dbPassword, driver = driverClassName)

    // create tables if they don't exist
    transaction {
        SchemaUtils.create(
            UserTable,
            UserProfilesTable,
            EventTable,
            EventAttendeeTable,
            PostTable,
            PostLikeTable,
            PostCommentTable,
            EventWaitingListTable,
            ImageHashTable,
            PasswordResetTable,
            ServiceTable,
            KidsServiceTable,
            KidsCheckInTable
        )
        SchemaUtils.createMissingTablesAndColumns(
            UserTable,
            UserProfilesTable,
            EventTable,
            EventAttendeeTable,
            PostTable,
            PostLikeTable,
            PostCommentTable,
            EventWaitingListTable,
            ImageHashTable,
            PasswordResetTable,
            ServiceTable,
            KidsServiceTable,
            KidsCheckInTable
        )
    }
}

/**
 * Makes a connection to a Postgres database.
 *
 * In order to connect to your running Postgres process,
 * please specify the following parameters in your configuration file:
 * - postgres.url -- Url of your running database process.
 * - postgres.user -- Username for database connection
 * - postgres.password -- Password for database connection
 *
 *  * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
 * and install Postgres and follow the instructions [here](https://postgresapp.com/).
 * Then, you would be able to edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
 * user and password values.
 *
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
 * your application shuts down by calling [Connection.close]
 * */
fun Application.connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
    if (embedded) {
        return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "root", "")
    } else {
        val url = environment.config.property("postgres.url").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        return DriverManager.getConnection(url, user, password)
    }
}
