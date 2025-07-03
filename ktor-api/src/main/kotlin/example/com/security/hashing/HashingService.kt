package example.com.security.hashing

interface HashingService {
    fun generateSaltedHash(password: String, length: Int = 32): SaltedHash
    fun verifySaltedHash(password: String, saltedHash: SaltedHash): Boolean
}