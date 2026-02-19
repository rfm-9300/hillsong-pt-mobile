package rfm.com.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rfm.com.service.FileStorageService
import java.net.URI

/**
 * Controller for serving uploaded files via S3/MinIO redirects
 */
@RestController
@RequestMapping("/api/files")
class FileController(
    private val fileStorageService: FileStorageService
) {
    
    private val logger = LoggerFactory.getLogger(FileController::class.java)
    
    /**
     * Serve uploaded files (Redirect to MinIO)
     */
    @GetMapping("/{subDirectory}/{fileName:.+}")
    fun serveFile(
        @PathVariable subDirectory: String,
        @PathVariable fileName: String
    ): ResponseEntity<Void> {
        return try {
            val filePath = "$subDirectory/$fileName"
            logger.debug("Redirecting to file: $filePath")
            
            if (!fileStorageService.fileExists(filePath)) {
                logger.warn("File not found in S3: $filePath")
                return ResponseEntity.notFound().build()
            }
            
            val presignedUrl = fileStorageService.getPresignedUrl(filePath)
            if (presignedUrl.isBlank()) {
                return ResponseEntity.notFound().build()
            }
            
            ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build()
                
        } catch (ex: Exception) {
            logger.error("Error redirecting to file: $subDirectory/$fileName", ex)
            ResponseEntity.internalServerError().build()
        }
    }
    
    /**
     * Serve files directly from root (Redirect to MinIO)
     */
    @GetMapping("/{fileName:.+}")
    fun serveRootFile(@PathVariable fileName: String): ResponseEntity<Void> {
        return try {
            logger.debug("Redirecting to root file: $fileName")
            
            if (!fileStorageService.fileExists(fileName)) {
                logger.warn("Root file not found in S3: $fileName")
                return ResponseEntity.notFound().build()
            }
            
            val presignedUrl = fileStorageService.getPresignedUrl(fileName)
            if (presignedUrl.isBlank()) {
                return ResponseEntity.notFound().build()
            }
            
            ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build()
                
        } catch (ex: Exception) {
            logger.error("Error redirecting to root file: $fileName", ex)
            ResponseEntity.internalServerError().build()
        }
    }
    
    /**
     * Get file information
     */
    @GetMapping("/{subDirectory}/{fileName:.+}/info")
    fun getFileInfo(
        @PathVariable subDirectory: String,
        @PathVariable fileName: String
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val filePath = "$subDirectory/$fileName"
            
            if (!fileStorageService.fileExists(filePath)) {
                return ResponseEntity.notFound().build()
            }
            
            val presignedUrl = fileStorageService.getPresignedUrl(filePath)
            
            val fileInfo = mapOf(
                "fileName" to fileName,
                "filePath" to filePath,
                "url" to presignedUrl,
                "exists" to true
            )
            
            ResponseEntity.ok(fileInfo)
            
        } catch (ex: Exception) {
            logger.error("Error getting file info: $subDirectory/$fileName", ex)
            ResponseEntity.internalServerError().build()
        }
    }
}