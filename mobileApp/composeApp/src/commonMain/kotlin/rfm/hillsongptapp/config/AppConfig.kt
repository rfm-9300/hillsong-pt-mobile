package rfm.hillsongptapp.config

/**
 * Application configuration interface
 * Platform-specific implementations provide build-time configuration values
 */
interface AppConfig {
    val apiBaseUrl: String
    val buildType: String
    val isDebug: Boolean
}

/**
 * Default configuration for platforms that don't have build config
 */
class DefaultAppConfig : AppConfig {
    override val apiBaseUrl: String = "https://activehive.pt:443"
    override val buildType: String = "release"
    override val isDebug: Boolean = false
}