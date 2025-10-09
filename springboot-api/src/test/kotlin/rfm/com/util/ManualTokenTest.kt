package rfm.com.util

import java.time.LocalDateTime

/**
 * Manual test runner to verify token generation and validation
 * Run this as a main function to test the utilities
 */
fun main() {
    println("=== Testing TokenGenerator ===")
    
    // Test 1: Generate tokens
    println("\n1. Generating 10 tokens:")
    val tokens = (1..10).map { TokenGenerator.generateSecureToken() }
    tokens.forEach { println("   Token: $it (length: ${it.length})") }
    
    // Test 2: Check uniqueness
    println("\n2. Checking uniqueness:")
    val uniqueTokens = tokens.toSet()
    println("   Generated: ${tokens.size}, Unique: ${uniqueTokens.size}")
    println("   All unique: ${tokens.size == uniqueTokens.size}")
    
    // Test 3: Check format
    println("\n3. Checking format:")
    tokens.forEach { token ->
        val isValid = TokenGenerator.isValidTokenFormat(token)
        println("   Token valid: $isValid")
    }
    
    // Test 4: Test invalid formats
    println("\n4. Testing invalid formats:")
    val invalidTokens = listOf(
        "short",
        "a".repeat(65),
        "a".repeat(63) + "!",
        ""
    )
    invalidTokens.forEach { token ->
        val isValid = TokenGenerator.isValidTokenFormat(token)
        println("   Token '$token' valid: $isValid (expected: false)")
    }
    
    println("\n=== Testing TokenValidator ===")
    
    // Test 5: Expiration check
    println("\n5. Testing expiration:")
    val futureTime = LocalDateTime.now().plusMinutes(15)
    val pastTime = LocalDateTime.now().minusMinutes(15)
    println("   Future time expired: ${TokenValidator.isTokenExpired(futureTime)} (expected: false)")
    println("   Past time expired: ${TokenValidator.isTokenExpired(pastTime)} (expected: true)")
    
    // Test 6: Token validation
    println("\n6. Testing token validation:")
    val validToken = TokenGenerator.generateSecureToken()
    println("   Valid token + future expiration: ${TokenValidator.validateToken(validToken, futureTime)} (expected: true)")
    println("   Valid token + past expiration: ${TokenValidator.validateToken(validToken, pastTime)} (expected: false)")
    println("   Invalid token + future expiration: ${TokenValidator.validateToken("short", futureTime)} (expected: false)")
    
    println("\n=== All Tests Complete ===")
}
