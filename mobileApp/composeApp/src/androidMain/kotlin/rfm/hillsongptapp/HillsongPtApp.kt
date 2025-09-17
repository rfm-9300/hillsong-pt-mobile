package rfm.hillsongptapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import rfm.hillsongptapp.di.initKoin

class HillsongPtApp: Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin{
            androidContext(this@HillsongPtApp)
            // Override API base URL with BuildConfig value for Android
            properties(
                mapOf(
                    "API_BASE_URL" to BuildConfig.API_BASE_URL
                )
            )
        }
    }


}