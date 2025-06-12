package rfm.hillsongptapp.core.data.repository.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse (
    val success: Boolean,
    val message: String,
    val data: LoginData,
)

@Serializable
data class LoginData (
    val type: String,
    val token: String,
)