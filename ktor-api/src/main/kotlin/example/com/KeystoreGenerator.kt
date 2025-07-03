package example.com

import java.io.File
import java.lang.ProcessBuilder

/**
 * Utility to generate a keystore for SSL/HTTPS
 */
object KeystoreGenerator {
    // Default path for the keystore file
    val KEYSTORE_PATH: String = File(System.getProperty("user.dir"), "keystore.jks").absolutePath
    
    /**
     * Generate a keystore at the specified path
     */
    fun generate(keystorePath: String = KEYSTORE_PATH): Boolean {
        val keystoreFile = File(keystorePath)
        
        try {
            println("Creating JKS keystore at: $keystorePath")
            
            // Make sure parent directory exists
            keystoreFile.parentFile?.mkdirs()
            
            // Delete existing keystore if it exists
            if (keystoreFile.exists()) {
                keystoreFile.delete()
                println("Deleted existing keystore file")
            }
            
            // Use keytool to generate the keystore (comes with JDK)
            val keytoolCmd = ProcessBuilder(
                "keytool", 
                "-genkeypair",
                "-alias", "sampleAlias",
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-validity", "365",
                "-keystore", keystorePath,
                "-storepass", "password123",
                "-keypass", "password123",
                "-dname", "CN=localhost, OU=Ktor, O=Ktor, L=Somewhere, S=Somewhere, C=US"
            )
            
            // Redirect error stream to output stream
            keytoolCmd.redirectErrorStream(true)
            
            // Start the process and wait for it to complete
            val process = keytoolCmd.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            // Check if keystore was created successfully
            if (exitCode == 0) {
                println("Keystore created successfully")
                println("File exists: ${keystoreFile.exists()}, size: ${keystoreFile.length()} bytes")
                return true
            } else {
                println("Error creating keystore, exit code: $exitCode")
                println("Output: $output")
                return false
            }
            
        } catch (e: Exception) {
            println("Error creating keystore: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Print instructions for manually creating a keystore
     */
    fun printInstructions(keystorePath: String = KEYSTORE_PATH) {
        println("WARNING: Keystore file not found at $keystorePath")
        println("Please run the keytool command to create it before starting the server:")
        println("""
            keytool -genkeypair \
              -alias sampleAlias \
              -keyalg RSA \
              -keysize 2048 \
              -validity 365 \
              -keystore "$keystorePath" \
              -storepass password123 \
              -keypass password123 \
              -dname "CN=localhost, OU=Ktor, O=Ktor, L=Somewhere, S=Somewhere, C=US"
        """.trimIndent())
    }
} 