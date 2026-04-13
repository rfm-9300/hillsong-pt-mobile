package rfm.hillsongptapp.config

/**
 * iOS-specific configuration
 * For iOS, we'll use a simple approach with compile-time constants
 * In a more advanced setup, you could read from Info.plist or use build settings
 */
class IosAppConfig : AppConfig {
    override val apiBaseUrl: String = "http://172.233.96.224:8080"
    override val buildType: String = "release"
    override val isDebug: Boolean = false
}
