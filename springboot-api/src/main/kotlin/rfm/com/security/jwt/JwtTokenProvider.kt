package rfm.com.security.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import rfm.com.config.SecurityLogger
import java.security.Key
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret}") private val jwtSecret: String,
    @Value("\${app.jwt.expiration}") private val jwtExpiration: Long,
    @Value("\${app.jwt.issuer:church-management-api}") private val jwtIssuer: String,
    @Value("\${app.jwt.audience:church-management-client}") private val jwtAudience: String
) {
    
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    /**
     * Generate JWT token from authentication
     */
    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserPrincipal
        return generateTokenFromUserId(userPrincipal.id, userPrincipal.username)
    }

    /**
     * Generate JWT token from user ID and email
     */
    fun generateTokenFromUserId(userId: Long, email: String): String {
        val expiryDate = Date(Date().time + jwtExpiration)
        
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(jwtIssuer)
            .audience().add(jwtAudience).and()
            .claim("email", email)
            .issuedAt(Date())
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * Generate verification token (24 hours expiry)
     */
    fun generateVerificationToken(userId: Long, email: String): String {
        val expiryDate = Date(Date().time + (24 * 60 * 60 * 1000)) // 24 hours
        
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(jwtIssuer)
            .audience().add(jwtAudience).and()
            .claim("email", email)
            .claim("type", "verification")
            .issuedAt(Date())
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * Generate refresh token (30 days expiry)
     */
    fun generateRefreshToken(userId: Long, email: String): String {
        val expiryDate = Date(Date().time + (30L * 24 * 60 * 60 * 1000)) // 30 days
        
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(jwtIssuer)
            .audience().add(jwtAudience).and()
            .claim("email", email)
            .claim("type", "refresh")
            .issuedAt(Date())
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * Get user ID from JWT token
     */
    fun getUserIdFromToken(token: String): Long {
        val claims = getClaimsFromToken(token)
        return claims.subject.toLong()
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
            val userId = claims.subject.toLongOrNull()
            val email = claims["email"] as? String
            
            // Check if token is expired
            if (claims.expiration.before(Date())) {
                logger.debug("JWT token is expired")
                SecurityLogger.logSecurityEvent(
                    event = SecurityLogger.SecurityEvent.TOKEN_VALIDATION_FAILURE,
                    userId = userId,
                    email = email,
                    details = mapOf("reason" to "expired")
                )
                return false
            }
            
            // Check issuer and audience
            if (claims.issuer != jwtIssuer) {
                logger.debug("JWT token has invalid issuer")
                SecurityLogger.logSecurityEvent(
                    event = SecurityLogger.SecurityEvent.TOKEN_VALIDATION_FAILURE,
                    userId = userId,
                    email = email,
                    details = mapOf("reason" to "invalid_issuer")
                )
                return false
            }
            
            // Log successful token validation
            SecurityLogger.logSecurityEvent(
                event = SecurityLogger.SecurityEvent.TOKEN_VALIDATION_SUCCESS,
                userId = userId,
                email = email
            )
            
            return true
        } catch (ex: SecurityException) {
            logger.error("Invalid JWT signature", ex)
            logTokenValidationFailure(token, "invalid_signature", ex)
        } catch (ex: MalformedJwtException) {
            logger.error("Invalid JWT token", ex)
            logTokenValidationFailure(token, "malformed_token", ex)
        } catch (ex: ExpiredJwtException) {
            logger.error("Expired JWT token", ex)
            logTokenValidationFailure(token, "expired_token", ex)
        } catch (ex: UnsupportedJwtException) {
            logger.error("Unsupported JWT token", ex)
            logTokenValidationFailure(token, "unsupported_token", ex)
        } catch (ex: IllegalArgumentException) {
            logger.error("JWT claims string is empty", ex)
            logTokenValidationFailure(token, "empty_claims", ex)
        } catch (ex: Exception) {
            logger.error("JWT token validation error", ex)
            logTokenValidationFailure(token, "unknown_error", ex)
        }
        return false
    }
    
    /**
     * Log token validation failure with details
     */
    private fun logTokenValidationFailure(token: String, reason: String, exception: Exception) {
        try {
            // Try to extract user info from token even if validation failed
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
            
            val userId = claims.subject.toLongOrNull()
            val email = claims["email"] as? String
            
            SecurityLogger.logSecurityEvent(
                event = SecurityLogger.SecurityEvent.TOKEN_VALIDATION_FAILURE,
                userId = userId,
                email = email,
                details = mapOf(
                    "reason" to reason,
                    "exception" to exception.javaClass.simpleName,
                    "message" to (exception.message ?: "No message")
                )
            )
        } catch (e: Exception) {
            // If we can't parse the token at all, log without user info
            SecurityLogger.logSecurityEvent(
                event = SecurityLogger.SecurityEvent.TOKEN_VALIDATION_FAILURE,
                details = mapOf(
                    "reason" to reason,
                    "exception" to exception.javaClass.simpleName,
                    "message" to (exception.message ?: "No message"),
                    "tokenParseError" to true
                )
            )
        }
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