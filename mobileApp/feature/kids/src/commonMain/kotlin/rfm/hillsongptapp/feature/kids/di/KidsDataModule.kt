package rfm.hillsongptapp.feature.kids.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rfm.hillsongptapp.feature.kids.data.database.datasource.KidsLocalDataSource
import rfm.hillsongptapp.feature.kids.data.database.datasource.KidsLocalDataSourceImpl
import rfm.hillsongptapp.feature.kids.data.network.datasource.KidsRemoteDataSource
import rfm.hillsongptapp.feature.kids.data.network.datasource.KidsRemoteDataSourceImpl
import rfm.hillsongptapp.feature.kids.data.network.websocket.RealTimeStatusManager
import rfm.hillsongptapp.feature.kids.data.repository.KidsRepositoryImpl
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository

/**
 * Koin module for Kids Management data layer dependencies
 * Note: This module is now included in the main featureKidsModule
 * @deprecated Use featureKidsModule instead
 */
@Deprecated("Use featureKidsModule instead", ReplaceWith("featureKidsModule"))
val kidsDataModule = module {
    
    // Local Data Source
    singleOf(::KidsLocalDataSourceImpl) bind KidsLocalDataSource::class
    
    // Remote Data Source
    singleOf(::KidsRemoteDataSourceImpl) bind KidsRemoteDataSource::class
    
    // Real-time Status Manager
    singleOf(::RealTimeStatusManager)
    
    // Repository
    singleOf(::KidsRepositoryImpl) bind KidsRepository::class
}