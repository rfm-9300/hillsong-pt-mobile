package rfm.hillsongptapp.feature.kids.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import rfm.hillsongptapp.feature.kids.data.database.dao.CheckInRecordDao
import rfm.hillsongptapp.feature.kids.data.database.dao.ChildDao
import rfm.hillsongptapp.feature.kids.data.database.dao.KidsServiceDao
import rfm.hillsongptapp.feature.kids.data.database.entity.CheckInRecordEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.ChildEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.KidsServiceEntity

/**
 * Room database for the Kids Management feature
 */
@Database(
    entities = [
        ChildEntity::class,
        KidsServiceEntity::class,
        CheckInRecordEntity::class
    ],
    version = 1,
    exportSchema = true
)
@ConstructedBy(KidsDatabaseConstructor::class)
abstract class KidsDatabase : RoomDatabase() {
    
    /**
     * Get the ChildDao for child-related database operations
     */
    abstract fun childDao(): ChildDao
    
    /**
     * Get the KidsServiceDao for service-related database operations
     */
    abstract fun kidsServiceDao(): KidsServiceDao
    
    /**
     * Get the CheckInRecordDao for check-in record database operations
     */
    abstract fun checkInRecordDao(): CheckInRecordDao
}

/**
 * Database file name for the kids database
 */
@Suppress("TopLevelPropertyNaming")
internal const val kidsDbFileName = "kids_management.db"

/**
 * Platform-specific database instance provider
 */
expect fun kidsDatabaseInstance(): KidsDatabase

/**
 * Database constructor for Room
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object KidsDatabaseConstructor : RoomDatabaseConstructor<KidsDatabase> {
    override fun initialize(): KidsDatabase
}