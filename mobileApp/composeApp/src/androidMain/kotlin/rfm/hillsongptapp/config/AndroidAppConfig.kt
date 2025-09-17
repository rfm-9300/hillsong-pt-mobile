package rfm.hillsongptapp.config

import rfm.hillsongptapp.BuildConfig

/**
 * Android-specific configuration that uses BuildConfig values
 */
class AndroidAppConfig : AppConfig {
    override val apiBaseUrl: String = BuildConfig.API_BASE_URL
    override val buildType: String = BuildConfig.BUILD_TYPE
    override val isDebug: Boolean = BuildConfig.DEBUG
}