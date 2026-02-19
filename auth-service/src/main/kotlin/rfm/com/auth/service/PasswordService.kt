package rfm.com.auth.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class PasswordService(
    private val passwordEncoder: PasswordEncoder
) {
    
    fun encodePassword(password: String): String {
        return passwordEncoder.encode(password)
    }
    
    fun verifyPassword(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }
}
