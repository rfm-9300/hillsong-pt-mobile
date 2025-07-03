package example.com.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class VerificationRequest (
    val token: String
)