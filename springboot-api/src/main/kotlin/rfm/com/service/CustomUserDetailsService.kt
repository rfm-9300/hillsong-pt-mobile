package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rfm.com.repository.UserRepository
import rfm.com.security.jwt.UserPrincipal

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmailWithProfile(email)
            ?: throw UsernameNotFoundException("User not found with email: $email")

        return UserPrincipal.create(user)
    }

    @Transactional(readOnly = true)
    fun loadUserById(id: Long): UserDetails {
        val user = userRepository.findByIdWithProfile(id)
            ?: throw UsernameNotFoundException("User not found with id: $id")
        
        return UserPrincipal.create(user)
    }
}