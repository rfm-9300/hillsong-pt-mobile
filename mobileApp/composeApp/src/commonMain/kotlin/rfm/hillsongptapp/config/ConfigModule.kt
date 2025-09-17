package rfm.hillsongptapp.config

import org.koin.dsl.module

val configModule = module {
    single<AppConfig> { getAppConfig() }
}

/**
 * Platform-specific function to get the appropriate AppConfig implementation
 * This will be implemented in each platform's source set
 */
expect fun getAppConfig(): AppConfig