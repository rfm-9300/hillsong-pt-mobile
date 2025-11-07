package rfm.com.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import rfm.com.service.CustomUserDetailsService
import rfm.com.security.jwt.UserPrincipal

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val customUserDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    
    // Master token for testing - bypasses all authentication
    // WARNING: Only use in development/testing environments!
    private val masterToken = System.getenv("MASTER_TEST_TOKEN") ?: "MASTER_TEST_TOKEN_CHANGE_ME"
    
    // Public endpoints that should skip JWT processing
    private val publicEndpoints = listOf(
        "/api/auth/",
        "/api/files/",
        "/api/health",
        "/actuator/",
        "/error",
        "/api/youtube-videos/active",
        "/api/encounters/upcoming",
        "/api/admin/"
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val requestURI = request.requestURI
            logger.info("🔍 JWT Filter executing for: ${request.method} $requestURI")
            
            // Skip JWT processing for public endpoints
            if (isPublicEndpoint(requestURI)) {
                logger.info("🚫 Skipping JWT processing for public endpoint: $requestURI")
                filterChain.doFilter(request, response)
                return
            }
            
            val jwt = getJwtFromRequest(request)
            
            logger.info("📝 Extracted JWT: ${jwt?.take(20)}... (length: ${jwt?.length})")
            logger.info("🔑 Master token: ${masterToken.take(20)}... (length: ${masterToken.length})")
            logger.info("✅ Tokens match: ${jwt == masterToken}")
            
            // Check for master token first (for testing)
            if (StringUtils.hasText(jwt) && jwt == masterToken) {
                logger.error("⚠️⚠️⚠️ MASTER TOKEN USED - Bypassing authentication for testing ⚠️⚠️⚠️")
                
                // Create a super admin authentication with all roles
                val authorities = listOf(
                    org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"),
                    org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"),
                    org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STAFF")
                )
                
                // Create a proper UserPrincipal with a valid user ID (999 for master test user)
                val masterUserPrincipal = UserPrincipal(
                    id = 4L,
                    email = "master-test@test.com",
                    password = "",
                    authorities = authorities,
                    verified = true,
                    enabled = true
                )
                
                val authentication = UsernamePasswordAuthenticationToken(
                    masterUserPrincipal,
                    null,
                    authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                
                SecurityContextHolder.getContext().authentication = authentication
                
                logger.error("✅ Set MASTER authentication with all roles: ${authorities.joinToString()}")
            }
            // Normal JWT validation
            else if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt!!)) {
                val userId = jwtTokenProvider.getUserIdFromToken(jwt)
                
                // Load user details with authorities from database
                val userDetails = customUserDetailsService.loadUserById(userId)
                
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                
                SecurityContextHolder.getContext().authentication = authentication
                
                logger.debug("Set authentication for user: ${userDetails.username}")
            }
        } catch (ex: Exception) {
            logger.error("Could not set user authentication in security context", ex)
        }
        
        filterChain.doFilter(request, response)
    }

    /**
     * Check if the request URI is a public endpoint that should skip JWT processing
     */
    private fun isPublicEndpoint(requestURI: String): Boolean {
        return publicEndpoints.any { endpoint ->
            requestURI.startsWith(endpoint) || requestURI == endpoint.trimEnd('/')
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}