package rfm.hillsongptapp.di
import org.koin.core.context.startKoin
import org.koin.core.lazyModules
import org.koin.dsl.KoinAppDeclaration
import rfm.hillsongptapp.feature.home.di.featureHomeModule
import rfm.hillsongptapp.core.data.di.dataModule
import rfm.hillsongptapp.feature.feed.di.featureFeedModule
import rfm.hillsongptapp.feature.login.di.featureLoginModule
import rfm.hillsongptapp.feature.login.di.koinPlatformModule

val featureModules = listOf(
    featureHomeModule,
    featureLoginModule,
    featureFeedModule
)

val coreModules = listOf(
    dataModule,
    koinPlatformModule
)

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(coreModules)
        lazyModules(featureModules)
    }
}