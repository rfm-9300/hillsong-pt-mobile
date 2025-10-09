package rfm.com.security.jwt

import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import rfm.com.entity.User
import rfm.com.service.OAuth2Service

/**
 * UserPrincipal represents the authenticated user in Spring Security context
 */
data class UserPrincipal(
    val id: Long,
    private val email: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>,
    val verified: Boolean = true,
    val enabled: Boolean = true
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String = password

    override fun getUsername(): String = email
    
    /**
     * Get the email address of the user
     */
    fun getEmail(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = enabled && verified


    companion object {
        private val logger = LoggerFactory.getLogger(OAuth2Service::class.java)
        /**
         * Create UserPrincipal from User entity
         */
        fun create(user: User): UserPrincipal {
            val authorities = mutableListOf<GrantedAuthority>()
            logger.debug("Creating UserPrincipal for user: ${user.email}")
            
            // Get roles from user_roles table
            val roleNames = user.getRoleNames()
            logger.debug("User roles from database: $roleNames")
            
            // Add authorities based on roles
            roleNames.forEach { roleName ->
                authorities.add(SimpleGrantedAuthority("ROLE_$roleName"))
                logger.debug("Added ROLE_$roleName for user: ${user.email}")
            }
            
            // Fallback: If no roles found in user_roles table, check legacy isAdmin flag
            if (authorities.isEmpty()) {
                logger.debug("No roles found in user_roles table, checking legacy isAdmin flag")
                authorities.add(SimpleGrantedAuthority("ROLE_USER"))
                logger.debug("Added ROLE_USER (fallback)")
                
                user.profile?.let { profile ->
                    if (profile.isAdmin) {
                        authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
                        logger.debug("Added ROLE_ADMIN from legacy flag for user: ${user.email}")
                    }
                }
            }
            
            logger.debug("Final authorities for ${user.email}: ${authorities.map { it.authority }}")
            
            return UserPrincipal(
                id = user.id!!,
                email = user.email,
                password = user.password,
                authorities = authorities,
                verified = user.verified,
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