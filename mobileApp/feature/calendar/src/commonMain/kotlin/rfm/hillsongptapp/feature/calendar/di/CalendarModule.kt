package rfm.hillsongptapp.feature.calendar.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.calendar.CalendarViewModel

val featureCalendarModule = lazyModule {
    viewModel<CalendarViewModel> {
        CalendarViewModel(
            calendarApiService = get()
        )
    }
}
