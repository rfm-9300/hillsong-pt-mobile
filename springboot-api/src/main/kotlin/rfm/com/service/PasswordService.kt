package rfm.com.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class PasswordService(
    private val passwordEncoder: PasswordEncoder
) {
    
    /**
     * Encode password using Spring Security's BCrypt encoder
     * BCrypt handles salting internally - no separate salt needed
     */
    fun encodePassword(password: String): String {
        return passwordEncoder.encode(password)
    }
    
    /**
     * Verify password using Spring Security's encoder
     * Works with BCrypt hashes that include internal salting
     */
    fun verifyPassword(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }
}