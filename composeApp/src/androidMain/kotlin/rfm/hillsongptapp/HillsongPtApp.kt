package rfm.hillsongptapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.lazyModules
import rfm.hillsongptapp.di.featureModules
import rfm.hillsongptapp.di.coreModules
import rfm.hillsongptapp.di.initKoin

class HillsongPtApp: Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin{
            androidContext(this@HillsongPtApp)
        }
    }


}