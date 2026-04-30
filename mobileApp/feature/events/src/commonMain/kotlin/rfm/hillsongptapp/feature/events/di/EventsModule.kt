package rfm.hillsongptapp.feature.events.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.events.EventDetailViewModel
import rfm.hillsongptapp.feature.events.EventsViewModel

val featureEventsModule = lazyModule {
    viewModel {
        EventsViewModel(
            eventsApiService = get(),
            baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl")),
        )
    }
    viewModel {
        EventDetailViewModel(
            eventsApiService = get(),
            baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl")),
        )
    }
}
