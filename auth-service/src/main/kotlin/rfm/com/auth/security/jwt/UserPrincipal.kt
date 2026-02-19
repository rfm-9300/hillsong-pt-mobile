package rfm.com.auth.security.jwt

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import rfm.com.auth.model.User
import com.fasterxml.jackson.annotation.JsonIgnore

data class UserPrincipal(
    val id: String,
    val email: String,
    @JsonIgnore val passwordHash: String,
    private val grantedAuthorities: Collection<GrantedAuthority>,
    val verified: Boolean = true,
    val enabled: Boolean = true
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = grantedAuthorities

    override fun getPassword(): String = passwordHash

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = enabled && verified

    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = user.roles.map { role ->
                SimpleGrantedAuthority("ROLE_${role.name}")
            }.toMutableList()

            if (authorities.isEmpty()) {
                authorities.add(SimpleGrantedAuthority("ROLE_USER"))
            }

            return UserPrincipal(
                id = user.id ?: "",
                email = user.email,
                passwordHash = user.password ?: "",
                grantedAuthorities = authorities,
                verified = user.verified,
                enabled = true // logic can be extended
            )
        }
    }
}
