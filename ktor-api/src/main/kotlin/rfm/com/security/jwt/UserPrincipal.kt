package rfm.com.security.jwt

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import rfm.com.entity.User

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
        /**
         * Create UserPrincipal from User entity
         */
        fun create(user: User): UserPrincipal {
            val authorities = mutableListOf<GrantedAuthority>()
            
            // Add default USER role
            authorities.add(SimpleGrantedAuthority("ROLE_USER"))
            
            // Add ADMIN role if user is admin
            user.profile?.let { profile ->
                if (profile.isAdmin) {
                    authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
                }
            }
            
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