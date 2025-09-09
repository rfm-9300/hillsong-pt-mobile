package rfm.hillsongptapp.core.network.provider

import io.ktor.client.engine.HttpClientEngine

expect fun httpClientEngine(): HttpClientEngine
