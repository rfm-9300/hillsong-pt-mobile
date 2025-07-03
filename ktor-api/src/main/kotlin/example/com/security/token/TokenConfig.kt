package example.com.security.token

data class TokenConfig(
    val issuer: String,
    val audience: String,
    val expiresInt: Long,
    val secret: String
)