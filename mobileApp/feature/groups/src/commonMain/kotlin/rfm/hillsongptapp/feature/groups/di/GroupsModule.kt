package rfm.hillsongptapp.feature.groups.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.groups.GroupsViewModel

val featureGroupsModule = lazyModule {
    viewModel {
        GroupsViewModel(
            groupsApiService = get(),
            baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl")),
        )
    }
}
