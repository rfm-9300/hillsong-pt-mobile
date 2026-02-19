package rfm.com.security.jwt

import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import rfm.com.entity.User

/**
 * UserPrincipal represents the authenticated user in Spring Security context.
 * Credentials are managed by auth-service; this only holds identity and roles.
 */
data class UserPrincipal(
    val id: String,
    val authId: String? = null,
    private val email: String,
    private val password: String = "",
    private val authorities: Collection<GrantedAuthority>,
    val verified: Boolean = true,
    val enabled: Boolean = true
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String = password

    override fun getUsername(): String = email
    
    fun getEmail(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = enabled && verified

    companion object {
        private val logger = LoggerFactory.getLogger(UserPrincipal::class.java)

        fun create(user: User): UserPrincipal {
            val authorities = mutableListOf<GrantedAuthority>()
            logger.debug("Creating UserPrincipal for user: ${user.email}")
            
            // Get roles directly from the user document
            val roles = user.roles
            logger.debug("User roles: $roles")
            
            roles.forEach { role ->
                authorities.add(SimpleGrantedAuthority("ROLE_${role.name}"))
                logger.debug("Added ROLE_${role.name} for user: ${user.email}")
            }
            
            // Fallback: if no roles, add USER role
            if (authorities.isEmpty()) {
                authorities.add(SimpleGrantedAuthority("ROLE_USER"))
                logger.debug("Added ROLE_USER (fallback) for user: ${user.email}")
                
                if (user.isAdmin) {
                    authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
                    logger.debug("Added ROLE_ADMIN from isAdmin flag for user: ${user.email}")
                }
            }
            
            logger.debug("Final authorities for ${user.email}: ${authorities.map { it.authority }}")
            
            return UserPrincipal(
                id = user.id!!,
                authId = user.authId,
                email = user.email,
                password = "",
                authorities = authorities,
                verified = true,
                enabled = true
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserPrincipal
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}