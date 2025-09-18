package rfm.hillsongptapp.core.network

import rfm.hillsongptapp.core.network.api.AuthApiService
import rfm.hillsongptapp.core.network.api.EventsApiService
import rfm.hillsongptapp.core.network.api.GroupsApiService
import rfm.hillsongptapp.core.network.api.PostsApiService
import rfm.hillsongptapp.core.network.api.PrayerApiService
import rfm.hillsongptapp.core.network.api.ProfileApiService

/**
 * Main API client that aggregates all feature-specific API services
 * Provides a single entry point for all network operations
 * 
 * Usage:
 * ```kotlin
 * class MyRepository(private val apiClient: HillsongApiClient) {
 *     suspend fun login(email: String, password: String) = 
 *         apiClient.auth.login(LoginRequest(email, password))
 * }
 * ```
 */
class HillsongApiClient(
    val auth: AuthApiService,
    val posts: PostsApiService,
    val profile: ProfileApiService,
    val events: EventsApiService,
    val groups: GroupsApiService,
    val prayer: PrayerApiService
) {
    companion object {
        const val API_VERSION = "v1"
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
    }
}