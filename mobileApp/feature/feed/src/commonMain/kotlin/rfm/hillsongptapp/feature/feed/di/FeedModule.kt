package rfm.hillsongptapp.feature.feed.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.feed.FeedViewModel

val featureFeedModule = lazyModule {
    viewModel<FeedViewModel> {
        FeedViewModel(
            postRepository = get(),
        )
    }
}
