package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import rfm.com.entity.User
import rfm.com.repository.UserRepository
import rfm.com.security.jwt.UserPrincipal

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    private val logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found with email: $email")
        return UserPrincipal.create(user)
    }

    /**
     * Loads a user by their authId (JWT subject).
     * If no user is found, auto-provisions one using the email from the JWT claims.
     * This allows users registered in auth-service to seamlessly access springboot-api
     * without requiring a separate registration step.
     */
    fun loadUserByAuthId(authId: String, email: String? = null): UserDetails {
        // Try finding by authId first
        val existingUser = userRepository.findByAuthId(authId)
            ?: userRepository.findById(authId).orElse(null)

        if (existingUser != null) {
            return UserPrincipal.create(existingUser)
        }

        // If not found and we have an email, try finding by email and linking the authId
        if (email != null) {
            val userByEmail = userRepository.findByEmail(email)
            if (userByEmail != null) {
                logger.info("🔗 Linking existing user (email: $email) to authId: $authId")
                val linked = userByEmail.copy(authId = authId)
                userRepository.save(linked)
                return UserPrincipal.create(linked)
            }

            // Auto-provision: create a new profile for this auth-service user
            logger.info("🆕 Auto-provisioning user profile for authId: $authId, email: $email")
            val newUser = User(
                authId = authId,
                email = email,
                firstName = email.substringBefore("@"), // placeholder until user sets profile
                lastName = ""
            )
            val saved = userRepository.save(newUser)
            return UserPrincipal.create(saved)
        }

        throw UsernameNotFoundException("User not found with authId: $authId")
    }
}