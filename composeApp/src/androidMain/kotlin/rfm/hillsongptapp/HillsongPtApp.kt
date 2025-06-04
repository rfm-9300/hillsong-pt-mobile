package rfm.hillsongptapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.lazyModules
import rfm.hillsongptapp.di.featureModules

class HillsongPtApp: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@HillsongPtApp)
            lazyModules(featureModules)
        }
    }


}