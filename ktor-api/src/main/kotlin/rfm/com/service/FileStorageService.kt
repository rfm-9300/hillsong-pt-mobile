package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

/**
 * Service for handling file storage operations
 */
@Service
class FileStorageService(
    @Value("\${app.upload.path:./uploads}") private val uploadPath: String
) {
    
    private val logger = LoggerFactory.getLogger(FileStorageService::class.java)
    private val uploadDir: Path = Paths.get(uploadPath).toAbsolutePath().normalize()
    
    init {
        try {
            Files.createDirectories(uploadDir)
            logger.info("Upload directory created/verified at: $uploadDir")
        } catch (ex: Exception) {
            logger.error("Could not create upload directory: $uploadDir", ex)
            throw RuntimeException("Could not create upload directory!", ex)
        }
    }
    
    /**
     * Store a file and return the file path
     */
    fun storeFile(file: MultipartFile, subDirectory: String = ""): String {
        validateFile(file)
        
        val fileName = generateUniqueFileName(file.originalFilename ?: "file")
        val targetDir = if (subDirectory.isNotBlank()) {
            uploadDir.resolve(subDirectory)
        } else {
            uploadDir
        }
        
        try {
            Files.createDirectories(targetDir)
            val targetLocation = targetDir.resolve(fileName)
            
            // Normalize the path to prevent directory traversal attacks
            val normalizedPath = targetLocation.normalize()
            if (!normalizedPath.startsWith(uploadDir.normalize())) {
                throw SecurityException("Cannot store file outside upload directory")
            }
            
            Files.copy(file.inputStream, normalizedPath, StandardCopyOption.REPLACE_EXISTING)
            
            val relativePath = if (subDirectory.isNotBlank()) {
                "$subDirectory/$fileName"
            } else {
                fileName
            }
            
            logger.info("File stored successfully: $relativePath")
            return relativePath
            
        } catch (ex: IOException) {
            logger.error("Failed to store file: ${file.originalFilename}", ex)
            throw RuntimeException("Failed to store file", ex)
        }
    }
    
    /**
     * Store an event header image
     */
    fun storeEventImage(file: MultipartFile): String {
        return storeFile(file, "events")
    }
    
    /**
     * Store a post image
     */
    fun storePostImage(file: MultipartFile): String {
        return storeFile(file, "posts")
    }
    
    /**
     * Store a profile image
     */
    fun storeProfileImage(file: MultipartFile): String {
        return storeFile(file, "profiles")
    }
    
    /**
     * Delete a file
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            val path = uploadDir.resolve(filePath).normalize()
            
            // Security check to prevent deletion outside upload directory
            if (!path.startsWith(uploadDir.normalize())) {
                logger.warn("Attempted to delete file outside upload directory: $filePath")
                return false
            }
            
            val deleted = Files.deleteIfExists(path)
            if (deleted) {
                logger.info("File deleted successfully: $filePath")
            } else {
                logger.warn("File not found for deletion: $filePath")
            }
            deleted
        } catch (ex: IOException) {
            logger.error("Failed to delete file: $filePath", ex)
            false
        }
    }
    
    /**
     * Get the full path to a file
     */
    fun getFilePath(relativePath: String): Path {
        return uploadDir.resolve(relativePath).normalize()
    }
    
    /**
     * Check if a file exists
     */
    fun fileExists(relativePath: String): Boolean {
        val path = uploadDir.resolve(relativePath).normalize()
        return Files.exists(path) && path.startsWith(uploadDir.normalize())
    }
    
    /**
     * Get file size in bytes
     */
    fun getFileSize(relativePath: String): Long {
        return try {
            val path = uploadDir.resolve(relativePath).normalize()
            if (path.startsWith(uploadDir.normalize()) && Files.exists(path)) {
                Files.size(path)
            } else {
                -1L
            }
        } catch (ex: IOException) {
            logger.error("Failed to get file size: $relativePath", ex)
            -1L
        }
    }
    
    /**
     * Validate uploaded file
     */
    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("File is empty")
        }
        
        val fileName = file.originalFilename ?: ""
        if (fileName.contains("..")) {
            throw SecurityException("Filename contains invalid path sequence: $fileName")
        }
        
        // Validate file size (10MB max)
        val maxFileSize = 10 * 1024 * 1024L // 10MB
        if (file.size > maxFileSize) {
            throw IllegalArgumentException("File size exceeds maximum allowed size of ${maxFileSize / (1024 * 1024)}MB")
        }
        
        // Validate file type for images
        val contentType = file.contentType ?: ""
        val allowedImageTypes = setOf(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
        )
        
        if (contentType.startsWith("image/") && !allowedImageTypes.contains(contentType)) {
            throw IllegalArgumentException("Unsupported image type: $contentType")
        }
    }
    
    /**
     * Generate a unique filename using UUID
     */
    private fun generateUniqueFileName(originalFilename: String): String {
        val extension = getFileExtension(originalFilename)
        val uuid = UUID.randomUUID().toString()
        return if (extension.isNotBlank()) {
            "$uuid.$extension"
        } else {
            uuid
        }
    }
    
    /**
     * Extract file extension from filename
     */
    private fun getFileExtension(filename: String): String {
        val lastDotIndex = filename.lastIndexOf('.')
        return if (lastDotIndex > 0 && lastDotIndex < filename.length - 1) {
            filename.substring(lastDotIndex + 1).lowercase()
        } else {
            ""
        }
    }
}