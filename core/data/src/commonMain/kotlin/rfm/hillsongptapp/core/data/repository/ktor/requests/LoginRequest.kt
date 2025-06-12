package rfm.hillsongptapp.core.data.repository.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest (
    val email: String,
    val password: String,
)