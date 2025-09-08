package rfm.hillsongptapp.feature.kids.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckInChildUseCase
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckOutChildUseCase

/**
 * Koin module for Kids Management use case dependencies
 * Note: This module is now included in the main featureKidsModule
 * @deprecated Use featureKidsModule instead
 */
@Deprecated("Use featureKidsModule instead", ReplaceWith("featureKidsModule"))
val kidsUseCaseModule = module {
    
    // Use Cases
    factoryOf(::CheckInChildUseCase)
    factoryOf(::CheckOutChildUseCase)
}