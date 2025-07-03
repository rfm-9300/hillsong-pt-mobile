package example.com.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import example.com.data.db.user.TokenTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class JwtTokenService: TokenService {

    override fun generateAuthToken(config: TokenConfig, vararg claims: TokenClaim): String {
        var token = JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresInt))
        claims.forEach {
            token = token.withClaim(it.name, it.value)
        }
        return token.sign(Algorithm.HMAC256(config.secret))
    }

    override fun generateVerificationToken(config: TokenConfig, vararg claims: TokenClaim): String {
        val newConfig = config.copy(expiresInt = 1000L * 60L * 60L * 24L)
        var token = JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withExpiresAt(Date(System.currentTimeMillis() + newConfig.expiresInt))
        claims.forEach {
            token = token.withClaim(it.name, it.value)
        }
        return token.sign(Algorithm.HMAC256(config.secret))
    }

    override fun generateRefreshToken(config: TokenConfig, vararg claims: TokenClaim): String {
        // Refresh tokens typically live longer than auth tokens (e.g., 30 days)
        val refreshConfig = config.copy(expiresInt = 1000L * 60L * 60L * 24L * 30L) // 30 days
        var token = JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withExpiresAt(Date(System.currentTimeMillis() + refreshConfig.expiresInt))
        claims.forEach {
            token = token.withClaim(it.name, it.value)
        }
        return token.sign(Algorithm.HMAC256(config.secret))
    }

    override fun validateToken(token: String, config: TokenConfig): Boolean {
        return try {
            val verifier = JWT.require(Algorithm.HMAC256(config.secret))
                .withIssuer(config.issuer)
                .withAudience(config.audience)
                .build()

            verifier.verify(token)
            true
        } catch (e: JWTVerificationException) {
            false
        }
    }

    override fun decodeToken(token: String): DecodedJWT? {
        return try {
            JWT.decode(token)
        } catch (e: JWTVerificationException) {
            null
        }
    }




}