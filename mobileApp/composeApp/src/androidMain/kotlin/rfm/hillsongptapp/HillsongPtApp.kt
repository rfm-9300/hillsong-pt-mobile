package rfm.hillsongptapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import rfm.hillsongptapp.di.initKoin
import rfm.hillsongptapp.util.platform.UrlOpener

class HillsongPtApp: Application() {

    override fun onCreate() {
        super.onCreate()
        UrlOpener.initialize(this)

        initKoin{
            androidContext(this@HillsongPtApp)
            // Override API base URL with BuildConfig value for Android
            properties(
                mapOf(
                    "API_BASE_URL" to BuildConfig.API_BASE_URL,
                    "AUTH_BASE_URL" to BuildConfig.AUTH_BASE_URL
                )
            )
        }
    }


}
