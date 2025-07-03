package example.com.security.token

import java.time.LocalDateTime

data class TokenPair(
    val id: Int,
    val userId: Int,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresAt: Long,
    val refreshTokenExpiresAt: Long,
    val isRevoked: Boolean = false,
    val deviceInfo: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastUsedAt: LocalDateTime = LocalDateTime.now()
)

data class RefreshResult(
    val userId: Int,
    val newAccessToken: String,
    val accessTokenExpiresAt: Long,
    val refreshToken: String? = null,
    val refreshTokenExpiresAt: Long? = null
)
