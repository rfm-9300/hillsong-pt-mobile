package rfm.com.service

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path

class FileStorageServiceTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var fileStorageService: FileStorageService

    @BeforeEach
    fun setUp() {
        fileStorageService = FileStorageService(tempDir.toString())
    }

    @Test
    fun `should store file successfully`() {
        // Given
        val mockFile = createMockFile("test.jpg", "image/jpeg", "test content")

        // When
        val result = fileStorageService.storeFile(mockFile)

        // Then
        assertNotNull(result)
        assertTrue(result.endsWith(".jpg"))
        assertTrue(fileStorageService.fileExists(result))
    }

    @Test
    fun `should store file in subdirectory`() {
        // Given
        val mockFile = createMockFile("test.png", "image/png", "test content")
        val subDirectory = "events"

        // When
        val result = fileStorageService.storeFile(mockFile, subDirectory)

        // Then
        assertNotNull(result)
        assertTrue(result.startsWith("events/"))
        assertTrue(result.endsWith(".png"))
        assertTrue(fileStorageService.fileExists(result))
    }

    @Test
    fun `should store event image`() {
        // Given
        val mockFile = createMockFile("event.jpg", "image/jpeg", "event content")

        // When
        val result = fileStorageService.storeEventImage(mockFile)

        // Then
        assertNotNull(result)
        assertTrue(result.startsWith("events/"))
        assertTrue(result.endsWith(".jpg"))
        assertTrue(fileStorageService.fileExists(result))
    }

    @Test
    fun `should store post image`() {
        // Given
        val mockFile = createMockFile("post.png", "image/png", "post content")

        // When
        val result = fileStorageService.storePostImage(mockFile)

        // Then
        assertNotNull(result)
        assertTrue(result.startsWith("posts/"))
        assertTrue(result.endsWith(".png"))
        assertTrue(fileStorageService.fileExists(result))
    }

    @Test
    fun `should store profile image`() {
        // Given
        val mockFile = createMockFile("profile.webp", "image/webp", "profile content")

        // When
        val result = fileStorageService.storeProfileImage(mockFile)

        // Then
        assertNotNull(result)
        assertTrue(result.startsWith("profiles/"))
        assertTrue(result.endsWith(".webp"))
        assertTrue(fileStorageService.fileExists(result))
    }

    @Test
    fun `should throw exception for empty file`() {
        // Given
        val mockFile = createMockFile("empty.jpg", "image/jpeg", "")
        every { mockFile.isEmpty } returns true

        // When & Then
        assertThrows<IllegalArgumentException> {
            fileStorageService.storeFile(mockFile)
        }
    }

    @Test
    fun `should throw exception for file with invalid path sequence`() {
        // Given
        val mockFile = createMockFile("../malicious.jpg", "image/jpeg", "content")

        // When & Then
        assertThrows<SecurityException> {
            fileStorageService.storeFile(mockFile)
        }
    }

    @Test
    fun `should throw exception for file exceeding size limit`() {
        // Given
        val mockFile = createMockFile("large.jpg", "image/jpeg", "content")
        every { mockFile.size } returns 11 * 1024 * 1024L // 11MB

        // When & Then
        assertThrows<IllegalArgumentException> {
            fileStorageService.storeFile(mockFile)
        }
    }

    @Test
    fun `should throw exception for unsupported image type`() {
        // Given
        val mockFile = createMockFile("test.bmp", "image/bmp", "content")

        // When & Then
        assertThrows<IllegalArgumentException> {
            fileStorageService.storeFile(mockFile)
        }
    }

    @Test
    fun `should delete file successfully`() {
        // Given
        val mockFile = createMockFile("test.jpg", "image/jpeg", "test content")
        val filePath = fileStorageService.storeFile(mockFile)

        // When
        val result = fileStorageService.deleteFile(filePath)

        // Then
        assertTrue(result)
        assertFalse(fileStorageService.fileExists(filePath))
    }

    @Test
    fun `should return false when deleting non-existent file`() {
        // Given
        val nonExistentPath = "non-existent/file.jpg"

        // When
        val result = fileStorageService.deleteFile(nonExistentPath)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should prevent deletion outside upload directory`() {
        // Given
        val maliciousPath = "../../../etc/passwd"

        // When
        val result = fileStorageService.deleteFile(maliciousPath)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should get correct file path`() {
        // Given
        val relativePath = "events/test.jpg"

        // When
        val result = fileStorageService.getFilePath(relativePath)

        // Then
        assertEquals(tempDir.resolve(relativePath).normalize(), result)
    }

    @Test
    fun `should check file existence correctly`() {
        // Given
        val mockFile = createMockFile("test.jpg", "image/jpeg", "test content")
        val filePath = fileStorageService.storeFile(mockFile)

        // When & Then
        assertTrue(fileStorageService.fileExists(filePath))
        assertFalse(fileStorageService.fileExists("non-existent.jpg"))
    }

    @Test
    fun `should get file size correctly`() {
        // Given
        val content = "test content"
        val mockFile = createMockFile("test.jpg", "image/jpeg", content)
        val filePath = fileStorageService.storeFile(mockFile)

        // When
        val size = fileStorageService.getFileSize(filePath)

        // Then
        assertEquals(content.length.toLong(), size)
    }

    @Test
    fun `should return -1 for non-existent file size`() {
        // Given
        val nonExistentPath = "non-existent.jpg"

        // When
        val size = fileStorageService.getFileSize(nonExistentPath)

        // Then
        assertEquals(-1L, size)
    }

    @Test
    fun `should generate unique filenames`() {
        // Given
        val mockFile1 = createMockFile("test.jpg", "image/jpeg", "content1")
        val mockFile2 = createMockFile("test.jpg", "image/jpeg", "content2")

        // When
        val path1 = fileStorageService.storeFile(mockFile1)
        val path2 = fileStorageService.storeFile(mockFile2)

        // Then
        assertNotEquals(path1, path2)
        assertTrue(path1.endsWith(".jpg"))
        assertTrue(path2.endsWith(".jpg"))
    }

    @Test
    fun `should handle file without extension`() {
        // Given
        val mockFile = createMockFile("testfile", "application/octet-stream", "content")

        // When
        val result = fileStorageService.storeFile(mockFile)

        // Then
        assertNotNull(result)
        assertFalse(result.contains("."))
        assertTrue(fileStorageService.fileExists(result))
    }

    private fun createMockFile(
        filename: String,
        contentType: String,
        content: String
    ): MultipartFile {
        val mockFile = mockk<MultipartFile>()
        every { mockFile.originalFilename } returns filename
        every { mockFile.contentType } returns contentType
        every { mockFile.isEmpty } returns content.isEmpty()
        every { mockFile.size } returns content.length.toLong()
        every { mockFile.inputStream } returns ByteArrayInputStream(content.toByteArray())
        return mockFile
    }
}