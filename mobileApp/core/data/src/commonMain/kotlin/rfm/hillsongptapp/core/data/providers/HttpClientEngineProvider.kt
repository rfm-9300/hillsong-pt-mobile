package rfm.hillsongptapp.core.data.providers

import io.ktor.client.engine.HttpClientEngine

expect fun httpClientEngine(): HttpClientEngine
