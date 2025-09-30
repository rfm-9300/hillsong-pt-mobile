package rfm.hillsongptapp.core.data.providers

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import rfm.hillsongptapp.core.data.repository.database.User
import rfm.hillsongptapp.core.data.repository.database.UserDao
import rfm.hillsongptapp.core.data.repository.database.UserProfile
import rfm.hillsongptapp.core.data.repository.database.UserProfileDao
import rfm.hillsongptapp.core.data.repository.database.ChildEntity
import rfm.hillsongptapp.core.data.repository.database.ChildDao
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordEntity
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordDao
import rfm.hillsongptapp.core.data.repository.database.KidsServiceEntity
import rfm.hillsongptapp.core.data.repository.database.KidsServiceDao

@Database(
    entities = [
        User::class, 
        UserProfile::class, 
        ChildEntity::class, 
        CheckInRecordEntity::class, 
        KidsServiceEntity::class
    ], 
    version = 3
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun childDao(): ChildDao
    abstract fun checkInRecordDao(): CheckInRecordDao
    abstract fun kidsServiceDao(): KidsServiceDao
}

@Suppress("TopLevelPropertyNaming")
internal const val dbFileName = "hillsongPTApp.db"

expect fun databaseInstance(): AppDatabase

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}