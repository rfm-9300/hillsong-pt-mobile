package rfm.hillsongptapp.di
import rfm.hillsongptapp.feature.home.di.featureHomeModule
import rfm.hillsongptapp.core.data.di.dataModule

val featureModules = listOf(
    featureHomeModule
)

val coreModules = listOf(
    dataModule
)