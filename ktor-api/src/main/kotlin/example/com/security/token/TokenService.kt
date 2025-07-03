package example.com.security.token

import com.auth0.jwt.interfaces.DecodedJWT


interface TokenService {
    fun generateAuthToken(config: TokenConfig, vararg claims: TokenClaim): String
    fun generateVerificationToken(config: TokenConfig, vararg claims: TokenClaim): String
    fun generateRefreshToken(config: TokenConfig, vararg claims: TokenClaim): String
    fun validateToken(token: String, config: TokenConfig): Boolean
    fun decodeToken(token: String): DecodedJWT?

}