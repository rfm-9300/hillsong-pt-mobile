package rfm.hillsongptapp.di
import rfm.hillsongptapp.feature.home.di.featureHomeModule
import rfm.hillsongptapp.core.data.di.dataModule
import rfm.hillsongptapp.feature.login.di.featureLoginModule

val featureModules = listOf(
    featureHomeModule,
    featureLoginModule
)

val coreModules = listOf(
    dataModule
)