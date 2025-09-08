package rfm.hillsongptapp.feature.kids.data.database

import androidx.room.Room
import org.koin.mp.KoinPlatform

/**
 * Android-specific implementation of the KidsDatabase instance
 */
actual fun kidsDatabaseInstance(): KidsDatabase {
    return Room.databaseBuilder(
        KoinPlatform.getKoin().get(),
        KidsDatabase::class.java,
        kidsDbFileName,
    ).build()
}