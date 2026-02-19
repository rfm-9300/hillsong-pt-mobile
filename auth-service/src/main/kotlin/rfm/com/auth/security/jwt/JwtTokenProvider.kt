package rfm.com.auth.security.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret}") private val jwtSecret: String,
    @Value("\${app.jwt.expiration}") private val jwtExpiration: Long,
    @Value("\${app.jwt.issuer:auth-service}") private val jwtIssuer: String,
    @Value("\${app.jwt.audience:church-management-client}") private val jwtAudience: String
) {
    
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserPrincipal
        return generateTokenFromUserId(userPrincipal.id, userPrincipal.email)
    }

    fun generateTokenFromUserId(userId: String, email: String): String {
        val expiryDate = Date(Date().time + jwtExpiration)
        
        return Jwts.builder()
            .subject(userId)
            .issuer(jwtIssuer)
            .audience().add(jwtAudience).and()
            .claim("email", email)
            .issuedAt(Date())
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getUserIdFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims.subject
    }

    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun validateToken(token: String): Boolean {
        try {
            val claims = getClaimsFromToken(token)
            
            if (claims.expiration.before(Date())) {
                logger.debug("JWT token is expired")
                return false
            }
            
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
        }
        return false
    }
}
