package rfm.hillsongptapp.feature.kids.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Database migrations for the Kids Management feature
 */
object KidsDatabaseMigrations {
    
    /**
     * Migration from version 1 to 2 (placeholder for future migrations)
     * This is an example of how to add migrations when the database schema changes
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            // Example migration - add a new column to children table
            // connection.execSQL("ALTER TABLE children ADD COLUMN newColumn TEXT")
        }
    }
    
    /**
     * Get all available migrations
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            // Add migrations here as they are created
            // MIGRATION_1_2
        )
    }
}

/**
 * Helper function to create indices for better query performance
 */
internal fun createIndices(connection: SQLiteConnection) {
    // Children table indices
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_children_parentId ON children(parentId)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_children_status ON children(status)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_children_currentServiceId ON children(currentServiceId)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_children_dateOfBirth ON children(dateOfBirth)")
    
    // Kids services table indices
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_kids_services_minAge_maxAge ON kids_services(minAge, maxAge)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_kids_services_isAcceptingCheckIns ON kids_services(isAcceptingCheckIns)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_kids_services_capacity ON kids_services(currentCapacity, maxCapacity)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_kids_services_time ON kids_services(startTime, endTime)")
    
    // Check-in records table indices
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_checkin_records_childId ON checkin_records(childId)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_checkin_records_serviceId ON checkin_records(serviceId)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_checkin_records_checkInTime ON checkin_records(checkInTime)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_checkin_records_checkOutTime ON checkin_records(checkOutTime)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_checkin_records_status ON checkin_records(status)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_checkin_records_checkedInBy ON checkin_records(checkedInBy)")
    connection.execSQL("CREATE INDEX IF NOT EXISTS index_checkin_records_checkedOutBy ON checkin_records(checkedOutBy)")
}