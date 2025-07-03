package example.com.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class FacebookAuthRequest(
    val accessToken: String
) 