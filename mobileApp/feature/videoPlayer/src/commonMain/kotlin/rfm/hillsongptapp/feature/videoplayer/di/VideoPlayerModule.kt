package rfm.hillsongptapp.feature.videoplayer.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.videoplayer.ui.VideoPlayerViewModel

val videoPlayerModule = lazyModule {
    viewModel { (videoId: Long, videoUrl: String) ->
        VideoPlayerViewModel(
            videoId = videoId,
            videoUrl = videoUrl,
            youtubeVideosApiService = get()
        )
    }
}
