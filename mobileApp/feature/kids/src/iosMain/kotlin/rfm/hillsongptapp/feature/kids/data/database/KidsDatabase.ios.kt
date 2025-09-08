package rfm.hillsongptapp.feature.kids.data.database

import androidx.room.Room
import androidx.room.util.findDatabaseConstructorAndInitDatabaseImpl
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * Get the iOS documents directory path
 */
@OptIn(ExperimentalForeignApi::class)
private fun fileDirectory(): String {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory).path!!
}

/**
 * iOS-specific implementation of the KidsDatabase instance
 */
actual fun kidsDatabaseInstance(): KidsDatabase {
    val dbFile = "${fileDirectory()}/$kidsDbFileName"
    return Room.databaseBuilder<KidsDatabase>(
        name = dbFile,
        factory = { findDatabaseConstructorAndInitDatabaseImpl(KidsDatabase::class) },
    ).setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}