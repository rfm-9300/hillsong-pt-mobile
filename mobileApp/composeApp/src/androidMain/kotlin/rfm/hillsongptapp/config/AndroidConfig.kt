package rfm.hillsongptapp.config

/**
 * Android-specific implementation of getAppConfig
 */
actual fun getAppConfig(): AppConfig = AndroidAppConfig()