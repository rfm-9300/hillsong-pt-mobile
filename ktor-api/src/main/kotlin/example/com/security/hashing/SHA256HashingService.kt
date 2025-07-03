package example.com.security.hashing

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

class SHA256HashingService: HashingService {

    override fun generateSaltedHash(password: String, length: Int): SaltedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(length)
        val saltAsHex = Hex.encodeHexString(salt)
        val hash = DigestUtils.sha256Hex("$password$saltAsHex")
        return SaltedHash(
            salt = Hex.encodeHexString(salt),
            hash = hash
        )
    }

    override fun verifySaltedHash(password: String, saltedHash: SaltedHash): Boolean {
        return DigestUtils.sha256Hex(password + saltedHash.salt) == saltedHash.hash
    }

}