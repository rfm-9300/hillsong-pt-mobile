package rfm.com.security.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT Token Provider — validates tokens issued by auth-service.
 * This API never generates JWTs; it only validates and extracts claims.
 */
@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret}")
    private val jwtSecret: String,

    @Value("\${app.jwt.issuer:auth-service}")
    private val jwtIssuer: String,

    @Value("\${app.jwt.audience:church-management-client}")
    private val jwtAudience: String
) {
    
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    // Key for verifying JWTs — must match auth-service key derivation (raw bytes, not base64)
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    /**
     * Get user ID from JWT token. Returns String because subject can be UUID string or Long string.
     */
    fun getUserIdFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims.subject
    }

    /**
     * Get email from JWT token
     */
    fun getEmailFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims["email"] as String
    }

    /**
     * Get token type from JWT token
     */
    fun getTokenTypeFromToken(token: String): String? {
        val claims = getClaimsFromToken(token)
        return claims["type"] as String?
    }

    /**
     * Get claims from JWT token
     */
    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * Validate JWT token
     */
    fun validateToken(token: String): Boolean {
        try {
            val claims = getClaimsFromToken(token)
            val userId = claims.subject 
            val email = claims["email"] as? String
            
            // Check if token is expired
            if (claims.expiration.before(Date())) {
                logger.debug("JWT token is expired")
                return false
            }
            
            // Check issuer and audience
            if (claims.issuer != jwtIssuer) {
                logger.debug("JWT token has invalid issuer")
                return false
            }
            
            return true
        } catch (ex: SecurityException) {
            logger.error("Invalid JWT signature", ex)
        } catch (ex: MalformedJwtException) {
            logger.error("Invalid JWT token", ex)
        } catch (ex: ExpiredJwtException) {
            logger.error("Expired JWT token", ex)
        } catch (ex: UnsupportedJwtException) {
            logger.error("Unsupported JWT token", ex)
        } catch (ex: IllegalArgumentException) {
            logger.error("JWT claims string is empty", ex)
        } catch (ex: Exception) {
            logger.error("JWT token validation error", ex)
        }
        return false
    }
    
    /**
     * Get expiration date from JWT token
     */
    fun getExpirationDateFromToken(token: String): Date {
        val claims = getClaimsFromToken(token)
        return claims.expiration
    }

    /**
     * Check if token is expired
     */
    fun isTokenExpired(token: String): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }
}