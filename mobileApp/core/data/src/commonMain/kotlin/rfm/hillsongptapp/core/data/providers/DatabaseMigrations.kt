package rfm.hillsongptapp.core.data.providers

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Database migration from version 2 to 3
 * Adds kids-related tables: children, kids_services, check_in_records
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        // Create children table
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS children (
                id TEXT NOT NULL PRIMARY KEY,
                parentId TEXT NOT NULL,
                name TEXT NOT NULL,
                dateOfBirth TEXT NOT NULL,
                medicalInfo TEXT,
                dietaryRestrictions TEXT,
                emergencyContact TEXT NOT NULL,
                status TEXT NOT NULL,
                currentServiceId TEXT,
                checkInTime TEXT,
                checkOutTime TEXT,
                createdAt TEXT NOT NULL,
                updatedAt TEXT NOT NULL
            )
        """.trimIndent())
        
        // Create kids_services table
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS kids_services (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                minAge INTEGER NOT NULL,
                maxAge INTEGER NOT NULL,
                startTime TEXT NOT NULL,
                endTime TEXT NOT NULL,
                location TEXT NOT NULL,
                maxCapacity INTEGER NOT NULL,
                currentCapacity INTEGER NOT NULL,
                isAcceptingCheckIns INTEGER NOT NULL,
                staffMembers TEXT NOT NULL,
                createdAt TEXT NOT NULL
            )
        """.trimIndent())
        
        // Create check_in_records table
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS check_in_records (
                id TEXT NOT NULL PRIMARY KEY,
                childId TEXT NOT NULL,
                serviceId TEXT NOT NULL,
                checkInTime TEXT NOT NULL,
                checkOutTime TEXT,
                checkedInBy TEXT NOT NULL,
                checkedOutBy TEXT,
                notes TEXT,
                status TEXT NOT NULL,
                FOREIGN KEY (childId) REFERENCES children(id) ON DELETE CASCADE,
                FOREIGN KEY (serviceId) REFERENCES kids_services(id) ON DELETE CASCADE
            )
        """.trimIndent())
        
        // Create indices for children table
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_children_parentId ON children(parentId)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_children_status ON children(status)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_children_currentServiceId ON children(currentServiceId)")
        
        // Create indices for kids_services table
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_kids_services_name ON kids_services(name)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_kids_services_startTime ON kids_services(startTime)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_kids_services_endTime ON kids_services(endTime)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_kids_services_isAcceptingCheckIns ON kids_services(isAcceptingCheckIns)")
        
        // Create indices for check_in_records table
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_check_in_records_childId ON check_in_records(childId)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_check_in_records_serviceId ON check_in_records(serviceId)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_check_in_records_checkInTime ON check_in_records(checkInTime)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_check_in_records_status ON check_in_records(status)")
    }
}