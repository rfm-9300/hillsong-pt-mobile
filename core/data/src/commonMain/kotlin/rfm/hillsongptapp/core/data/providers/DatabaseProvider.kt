package rfm.hillsongptapp.core.data.providers

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import rfm.hillsongptapp.core.data.repository.database.User
import rfm.hillsongptapp.core.data.repository.database.UserDao

@Database(entities = [User::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}

@Suppress("TopLevelPropertyNaming")
internal const val dbFileName = "hillsongPTApp.db"

expect fun databaseInstance(): AppDatabase

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}