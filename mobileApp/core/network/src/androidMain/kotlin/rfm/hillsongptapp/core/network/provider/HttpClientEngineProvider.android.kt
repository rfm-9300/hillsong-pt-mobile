package rfm.hillsongptapp.core.network.provider

import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.engine.okhttp.OkHttpEngine

actual fun httpClientEngine(): io.ktor.client.engine.HttpClientEngine {
    return OkHttpEngine(OkHttpConfig())
}