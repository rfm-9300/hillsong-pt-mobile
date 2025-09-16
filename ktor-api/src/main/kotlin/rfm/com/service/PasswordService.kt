package rfm.com.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import rfm.com.security.hashing.SaltedHash
import java.security.SecureRandom
import java.util.*

@Service
class PasswordService(
    private val passwordEncoder: PasswordEncoder
) {
    
    private val secureRandom = SecureRandom()
    
    /**
     * Generate salted hash using Spring Security's BCrypt encoder
     */
    fun generateSaltedHash(password: String, saltLength: Int = 32): SaltedHash {
        val salt = generateSalt(saltLength)
        val hash = passwordEncoder.encode(password + salt)
        return SaltedHash(hash = hash, salt = salt)
    }
    
    /**
     * Verify password against salted hash using Spring Security's encoder
     */
    fun verifySaltedHash(password: String, saltedHash: SaltedHash): Boolean {
        return passwordEncoder.matches(password + saltedHash.salt, saltedHash.hash)
    }
    
    /**
     * Encode password using Spring Security's BCrypt encoder (recommended for new implementations)
     */
    fun encodePassword(password: String): String {
        return passwordEncoder.encode(password)
    }
    
    /**
     * Verify password using Spring Security's encoder (recommended for new implementations)
     */
    fun verifyPassword(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }
    
    /**
     * Generate random salt
     */
    private fun generateSalt(length: Int): String {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }
}