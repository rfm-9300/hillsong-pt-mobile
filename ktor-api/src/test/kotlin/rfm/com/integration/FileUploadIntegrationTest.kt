package rfm.com.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import rfm.com.entity.AuthProvider
import rfm.com.repository.UserRepository
import rfm.com.repository.UserProfileRepository
import rfm.com.security.jwt.JwtTokenProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Integration test for file upload and serving functionality.
 * Validates that the Spring Boot implementation maintains compatibility
 * with the original Ktor file handling behavior.
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:filetest",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.upload.path=./test-uploads",
    "logging.level.rfm.com=INFO"
])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FileUploadIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userProfileRepository: UserProfileRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var testUser: User
    private lateinit var jwtToken: String

    @BeforeEach
    fun setUp() {
        // Create test upload directory
        val uploadDir = File("./test-uploads")
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
        }

        // Create test user for authentication
        testUser = User(
            email = "filetest@example.com",
            password = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.IjZg.BkmBm32S.6ZQuMjA/3oUYeizC",
            salt = "test-salt",
            verified = true,
            authProvider = AuthProvider.LOCAL
        )
        testUser = userRepository.save(testUser)

        val testUserProfile = UserProfile(
            user = testUser,
            firstName = "File",
            lastName = "Tester",
            email = testUser.email,
            phone = "1234567890"
        )
        userProfileRepository.save(testUserProfile)

        jwtToken = jwtTokenProvider.generateTokenFromUserId(testUser.id!!, testUser.email)
    }

    @AfterEach
    fun tearDown() {
        userProfileRepository.deleteAll()
        userRepository.deleteAll()
        
        // Clean up test upload directory
        val uploadDir = File("./test-uploads")
        if (uploadDir.exists()) {
            uploadDir.listFiles()?.forEach { it.delete() }
            uploadDir.delete()
        }
    }

    @Test
    @Order(1)
    fun `should upload JPEG image successfully`() {
        val imageFile = MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            createTestImageBytes()
        )

        mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("File uploaded successfully"))
        .andExpect(jsonPath("$.data.fileName").exists())
        .andExpect(jsonPath("$.data.filePath").exists())
        .andExpect(jsonPath("$.data.fileSize").exists())
        .andExpect(jsonPath("$.data.contentType").value("image/jpeg"))
    }

    @Test
    @Order(2)
    fun `should upload PNG image successfully`() {
        val imageFile = MockMultipartFile(
            "file",
            "test-image.png",
            "image/png",
            createTestImageBytes()
        )

        mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("File uploaded successfully"))
        .andExpect(jsonPath("$.data.contentType").value("image/png"))
    }

    @Test
    @Order(3)
    fun `should upload WebP image successfully`() {
        val imageFile = MockMultipartFile(
            "file",
            "test-image.webp",
            "image/webp",
            createTestImageBytes()
        )

        mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("File uploaded successfully"))
        .andExpect(jsonPath("$.data.contentType").value("image/webp"))
    }

    @Test
    @Order(4)
    fun `should reject non-image file upload`() {
        val textFile = MockMultipartFile(
            "file",
            "document.txt",
            "text/plain",
            "This is a text document".toByteArray()
        )

        mockMvc.perform(
            multipart("/api/files/upload")
                .file(textFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Only image files are allowed"))
    }

    @Test
    @Order(5)
    fun `should reject file upload without authentication`() {
        val imageFile = MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            createTestImageBytes()
        )

        mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile)
        )
        .andExpect(status().isUnauthorized)
    }

    @Test
    @Order(6)
    fun `should reject oversized file upload`() {
        // Create a large file (simulate > 10MB)
        val largeContent = ByteArray(11 * 1024 * 1024) // 11MB
        val largeFile = MockMultipartFile(
            "file",
            "large-image.jpg",
            "image/jpeg",
            largeContent
        )

        mockMvc.perform(
            multipart("/api/files/upload")
                .file(largeFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isBadRequest)
    }

    @Test
    @Order(7)
    fun `should serve uploaded file correctly`() {
        // First upload a file
        val imageFile = MockMultipartFile(
            "file",
            "serve-test.jpg",
            "image/jpeg",
            createTestImageBytes()
        )

        val uploadResult = mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andReturn()

        val uploadResponse = objectMapper.readTree(uploadResult.response.contentAsString)
        val fileName = uploadResponse.get("data").get("fileName").asText()

        // Then serve the file
        mockMvc.perform(get("/api/files/$fileName"))
        .andExpect(status().isOk)
        .andExpect(header().string("Content-Type", "image/jpeg"))
        .andExpect(header().exists("Content-Length"))
        .andExpect(content().bytes(createTestImageBytes()))
    }

    @Test
    @Order(8)
    fun `should return 404 for non-existent file`() {
        mockMvc.perform(get("/api/files/non-existent-file.jpg"))
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("File not found"))
    }

    @Test
    @Order(9)
    fun `should handle file upload with special characters in filename`() {
        val imageFile = MockMultipartFile(
            "file",
            "test image with spaces & symbols!.jpg",
            "image/jpeg",
            createTestImageBytes()
        )

        mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.fileName").exists())
        // Verify the filename is sanitized (UUID-based naming)
        .andExpect(jsonPath("$.data.fileName").value(org.hamcrest.Matchers.matchesPattern("^[a-f0-9-]+\\.jpg$")))
    }

    @Test
    @Order(10)
    fun `should generate unique filenames for duplicate uploads`() {
        val imageFile1 = MockMultipartFile(
            "file",
            "duplicate.jpg",
            "image/jpeg",
            createTestImageBytes()
        )

        val imageFile2 = MockMultipartFile(
            "file",
            "duplicate.jpg",
            "image/jpeg",
            createTestImageBytes()
        )

        // Upload first file
        val result1 = mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile1)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andReturn()

        // Upload second file with same name
        val result2 = mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile2)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andReturn()

        // Verify different filenames were generated
        val response1 = objectMapper.readTree(result1.response.contentAsString)
        val response2 = objectMapper.readTree(result2.response.contentAsString)
        
        val fileName1 = response1.get("data").get("fileName").asText()
        val fileName2 = response2.get("data").get("fileName").asText()
        
        Assertions.assertNotEquals(fileName1, fileName2, "Duplicate uploads should generate unique filenames")
    }

    @Test
    @Order(11)
    fun `should handle empty file upload gracefully`() {
        val emptyFile = MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            ByteArray(0)
        )

        mockMvc.perform(
            multipart("/api/files/upload")
                .file(emptyFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("File cannot be empty"))
    }

    @Test
    @Order(12)
    fun `should validate file extension matches content type`() {
        // Upload a file with mismatched extension and content type
        val imageFile = MockMultipartFile(
            "file",
            "test.txt", // .txt extension
            "image/jpeg", // but claiming to be JPEG
            createTestImageBytes()
        )

        mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("File extension does not match content type"))
    }

    @Test
    @Order(13)
    fun `should set proper cache headers for served files`() {
        // Upload a file first
        val imageFile = MockMultipartFile(
            "file",
            "cache-test.jpg",
            "image/jpeg",
            createTestImageBytes()
        )

        val uploadResult = mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andReturn()

        val uploadResponse = objectMapper.readTree(uploadResult.response.contentAsString)
        val fileName = uploadResponse.get("data").get("fileName").asText()

        // Serve the file and check cache headers
        mockMvc.perform(get("/api/files/$fileName"))
        .andExpect(status().isOk)
        .andExpect(header().exists("Cache-Control"))
        .andExpect(header().exists("ETag"))
    }

    private fun createTestImageBytes(): ByteArray {
        // Create a minimal valid JPEG header for testing
        return byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(), // JPEG SOI marker
            0xFF.toByte(), 0xE0.toByte(), // JFIF marker
            0x00, 0x10, // Length
            0x4A, 0x46, 0x49, 0x46, 0x00, // "JFIF\0"
            0x01, 0x01, // Version
            0x01, // Units
            0x00, 0x48, 0x00, 0x48, // X and Y density
            0x00, 0x00, // Thumbnail dimensions
            0xFF.toByte(), 0xD9.toByte() // JPEG EOI marker
        )
    }
}