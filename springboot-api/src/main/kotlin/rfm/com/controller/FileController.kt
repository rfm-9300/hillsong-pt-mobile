package rfm.com.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rfm.com.service.FileStorageService

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
     * Serve uploaded files by streaming bytes directly from MinIO.
     * Using direct streaming instead of redirect so that clients
     * (e.g. Android) do not need access to the internal MinIO hostname.
     */
    @GetMapping("/{subDirectory}/{fileName:.+}")
    fun serveFile(
        @PathVariable subDirectory: String,
        @PathVariable fileName: String
    ): ResponseEntity<ByteArray> {
        val filePath = "$subDirectory/$fileName"
        logger.debug("Serving file: $filePath")

        val fileDownload = fileStorageService.downloadFile(filePath)
            ?: run {
                logger.warn("File not found in S3: $filePath")
                return ResponseEntity.notFound().build()
            }

        val headers = HttpHeaders()
        headers.contentType = try {
            MediaType.parseMediaType(fileDownload.contentType)
        } catch (_: Exception) {
            MediaType.APPLICATION_OCTET_STREAM
        }
        headers.contentLength = fileDownload.bytes.size.toLong()

        return ResponseEntity(fileDownload.bytes, headers, HttpStatus.OK)
    }
    
    /**
     * Serve files directly from root by streaming bytes.
     */
    @GetMapping("/{fileName:.+}")
    fun serveRootFile(@PathVariable fileName: String): ResponseEntity<ByteArray> {
        logger.debug("Serving root file: $fileName")

        val fileDownload = fileStorageService.downloadFile(fileName)
            ?: run {
                logger.warn("Root file not found in S3: $fileName")
                return ResponseEntity.notFound().build()
            }

        val headers = HttpHeaders()
        headers.contentType = try {
            MediaType.parseMediaType(fileDownload.contentType)
        } catch (_: Exception) {
            MediaType.APPLICATION_OCTET_STREAM
        }
        headers.contentLength = fileDownload.bytes.size.toLong()

        return ResponseEntity(fileDownload.bytes, headers, HttpStatus.OK)
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