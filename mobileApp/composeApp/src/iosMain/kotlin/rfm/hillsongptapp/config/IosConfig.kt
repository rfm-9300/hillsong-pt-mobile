package rfm.hillsongptapp.config

/**
 * iOS-specific implementation of getAppConfig
 */
actual fun getAppConfig(): AppConfig = IosAppConfig()