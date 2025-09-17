package rfm.com.controller

import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rfm.com.service.FileStorageService
import java.nio.file.Files

/**
 * Controller for serving uploaded files
 */
@RestController
@RequestMapping("/api/files")
class FileController(
    private val fileStorageService: FileStorageService
) {
    
    private val logger = LoggerFactory.getLogger(FileController::class.java)
    
    /**
     * Serve uploaded files
     */
    @GetMapping("/{subDirectory}/{fileName:.+}")
    fun serveFile(
        @PathVariable subDirectory: String,
        @PathVariable fileName: String
    ): ResponseEntity<Resource> {
        return try {
            val filePath = "$subDirectory/$fileName"
            logger.debug("Serving file: $filePath")
            
            if (!fileStorageService.fileExists(filePath)) {
                logger.warn("File not found: $filePath")
                return ResponseEntity.notFound().build()
            }
            
            val file = fileStorageService.getFilePath(filePath)
            val resource: Resource = UrlResource(file.toUri())
            
            if (!resource.exists() || !resource.isReadable) {
                logger.warn("File not readable: $filePath")
                return ResponseEntity.notFound().build()
            }
            
            // Determine content type
            val contentType = try {
                Files.probeContentType(file) ?: "application/octet-stream"
            } catch (ex: Exception) {
                logger.warn("Could not determine file type for: $filePath", ex)
                "application/octet-stream"
            }
            
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$fileName\"")
                .body(resource)
                
        } catch (ex: Exception) {
            logger.error("Error serving file: $subDirectory/$fileName", ex)
            ResponseEntity.internalServerError().build()
        }
    }
    
    /**
     * Serve files directly from root upload directory (for backward compatibility)
     */
    @GetMapping("/{fileName:.+}")
    fun serveRootFile(@PathVariable fileName: String): ResponseEntity<Resource> {
        return try {
            logger.debug("Serving root file: $fileName")
            
            if (!fileStorageService.fileExists(fileName)) {
                logger.warn("Root file not found: $fileName")
                return ResponseEntity.notFound().build()
            }
            
            val file = fileStorageService.getFilePath(fileName)
            val resource: Resource = UrlResource(file.toUri())
            
            if (!resource.exists() || !resource.isReadable) {
                logger.warn("Root file not readable: $fileName")
                return ResponseEntity.notFound().build()
            }
            
            // Determine content type
            val contentType = try {
                Files.probeContentType(file) ?: "application/octet-stream"
            } catch (ex: Exception) {
                logger.warn("Could not determine file type for: $fileName", ex)
                "application/octet-stream"
            }
            
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$fileName\"")
                .body(resource)
                
        } catch (ex: Exception) {
            logger.error("Error serving root file: $fileName", ex)
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
            
            val file = fileStorageService.getFilePath(filePath)
            val size = fileStorageService.getFileSize(filePath)
            val contentType = Files.probeContentType(file) ?: "application/octet-stream"
            
            val fileInfo = mapOf(
                "fileName" to fileName,
                "filePath" to filePath,
                "size" to size,
                "contentType" to contentType,
                "exists" to true
            )
            
            ResponseEntity.ok(fileInfo)
            
        } catch (ex: Exception) {
            logger.error("Error getting file info: $subDirectory/$fileName", ex)
            ResponseEntity.internalServerError().build()
        }
    }
}