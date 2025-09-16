package rfm.com.controller

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rfm.com.service.FileStorageService
import java.nio.file.Paths

@WebMvcTest(FileController::class)
@ContextConfiguration(classes = [FileControllerTest.TestConfig::class])
class FileControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var fileStorageService: FileStorageService

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun fileStorageService(): FileStorageService = mockk()
    }

    @Test
    fun `should serve existing file successfully`() {
        // Given
        val subDirectory = "images"
        val fileName = "test.jpg"
        val filePath = "$subDirectory/$fileName"
        val tempFile = kotlin.io.path.createTempFile("test", ".jpg")
        tempFile.toFile().writeText("test image content")

        every { fileStorageService.fileExists(filePath) } returns true
        every { fileStorageService.getFilePath(filePath) } returns tempFile

        // When & Then
        mockMvc.perform(get("/api/files/$subDirectory/$fileName"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Disposition", "inline; filename=\"$fileName\""))
    }

    @Test
    fun `should return 404 for non-existent file`() {
        // Given
        val subDirectory = "images"
        val fileName = "nonexistent.jpg"
        val filePath = "$subDirectory/$fileName"

        every { fileStorageService.fileExists(filePath) } returns false

        // When & Then
        mockMvc.perform(get("/api/files/$subDirectory/$fileName"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should serve root file successfully`() {
        // Given
        val fileName = "logo.png"
        val tempFile = kotlin.io.path.createTempFile("logo", ".png")
        tempFile.toFile().writeText("logo content")

        every { fileStorageService.fileExists(fileName) } returns true
        every { fileStorageService.getFilePath(fileName) } returns tempFile

        // When & Then
        mockMvc.perform(get("/api/files/$fileName"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Disposition", "inline; filename=\"$fileName\""))
    }

    @Test
    fun `should return 404 for non-existent root file`() {
        // Given
        val fileName = "nonexistent.png"

        every { fileStorageService.fileExists(fileName) } returns false

        // When & Then
        mockMvc.perform(get("/api/files/$fileName"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return file info successfully`() {
        // Given
        val subDirectory = "images"
        val fileName = "test.jpg"
        val filePath = "$subDirectory/$fileName"
        val tempFile = kotlin.io.path.createTempFile("test", ".jpg")
        val fileSize = 1024L

        every { fileStorageService.fileExists(filePath) } returns true
        every { fileStorageService.getFilePath(filePath) } returns tempFile
        every { fileStorageService.getFileSize(filePath) } returns fileSize

        // When & Then
        mockMvc.perform(get("/api/files/$subDirectory/$fileName/info"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.fileName").value(fileName))
            .andExpect(jsonPath("$.filePath").value(filePath))
            .andExpect(jsonPath("$.size").value(fileSize))
            .andExpect(jsonPath("$.exists").value(true))
    }

    @Test
    fun `should return 404 for file info when file does not exist`() {
        // Given
        val subDirectory = "images"
        val fileName = "nonexistent.jpg"
        val filePath = "$subDirectory/$fileName"

        every { fileStorageService.fileExists(filePath) } returns false

        // When & Then
        mockMvc.perform(get("/api/files/$subDirectory/$fileName/info"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should handle file serving errors gracefully`() {
        // Given
        val subDirectory = "images"
        val fileName = "error.jpg"
        val filePath = "$subDirectory/$fileName"

        every { fileStorageService.fileExists(filePath) } throws RuntimeException("Storage error")

        // When & Then
        mockMvc.perform(get("/api/files/$subDirectory/$fileName"))
            .andExpect(status().isInternalServerError)
    }

    @Test
    fun `should handle file info errors gracefully`() {
        // Given
        val subDirectory = "images"
        val fileName = "error.jpg"
        val filePath = "$subDirectory/$fileName"

        every { fileStorageService.fileExists(filePath) } throws RuntimeException("Storage error")

        // When & Then
        mockMvc.perform(get("/api/files/$subDirectory/$fileName/info"))
            .andExpect(status().isInternalServerError)
    }
}