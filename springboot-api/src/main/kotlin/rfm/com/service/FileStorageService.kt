package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.URI
import java.time.Duration
import java.util.*

/**
 * Service for handling file storage operations using S3/MinIO
 */
@Service
class FileStorageService(
    private val s3Client: S3Client,
    @Value("\${app.minio.bucket}") private val bucketName: String,
    @Value("\${app.minio.endpoint}") private val endpoint: String,
    @Value("\${app.minio.access-key}") private val accessKey: String,
    @Value("\${app.minio.secret-key}") private val secretKey: String
) {
    
    private val logger = LoggerFactory.getLogger(FileStorageService::class.java)
    private val presigner: S3Presigner
    
    init {
        // Initialize presigner
        val credentials = software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(accessKey, secretKey)
        val staticCredentialsProvider = software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(credentials)
        
        presigner = S3Presigner.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(staticCredentialsProvider)
            .region(software.amazon.awssdk.regions.Region.US_EAST_1)
            .serviceConfiguration(
                software.amazon.awssdk.services.s3.S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build()
            )
            .build()
            
        initializeBucket()
    }
    
    private fun initializeBucket() {
        try {
            val bucketExists = try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build())
                true
            } catch (e: NoSuchBucketException) {
                false
            } catch (e: Exception) {
                // If 404 is wrapped
                if (e.message?.contains("404") == true) false else throw e
            }
            
            if (!bucketExists) {
                logger.info("Bucket $bucketName does not exist. Creating it...")
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
                logger.info("Bucket $bucketName created successfully")
                
                // Set bucket policy to allow public read access (optional, depending on requirements)
                // For now, we'll stick to presigned URLs or direct access if configured publicly
            } else {
                logger.info("Bucket $bucketName already exists")
            }
        } catch (ex: Exception) {
            logger.error("Could not initialize S3 bucket: $bucketName", ex)
            throw RuntimeException("Could not initialize storage!", ex)
        }
    }
    
    /**
     * Store a file and return the file path (key in S3)
     */
    fun storeFile(file: MultipartFile, subDirectory: String = ""): String {
        validateFile(file)
        
        val fileName = generateUniqueFileName(file.originalFilename ?: "file")
        val key = if (subDirectory.isNotBlank()) {
            "$subDirectory/$fileName"
        } else {
            fileName
        }
        
        try {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.contentType)
                .build()
                
            s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(file.inputStream, file.size)
            )
            
            logger.info("File stored successfully in S3: $key")
            return key
            
        } catch (ex: Exception) {
            logger.error("Failed to store file in S3: ${file.originalFilename}", ex)
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
     * Store an encounter image
     */
    fun storeEncounterImage(file: MultipartFile): String {
        return storeFile(file, "encounters")
    }
    
    /**
     * Delete a file
     */
    fun deleteFile(filePath: String): Boolean {
        if (filePath.isBlank()) return false
        
        return try {
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build()
            )
            logger.info("File deleted successfully from S3: $filePath")
            true
        } catch (ex: Exception) {
            logger.error("Failed to delete file from S3: $filePath", ex)
            false
        }
    }
    
    /**
     * Check if a file exists
     */
    fun fileExists(filePath: String): Boolean {
        if (filePath.isBlank()) return false
        
        return try {
            s3Client.headObject(
                HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build()
            )
            true
        } catch (e: NoSuchKeyException) {
            false
        } catch (e: Exception) {
            // If 404 is wrapped
            if (e.message?.contains("404") == true) false else {
                logger.warn("Error checking file existence: $filePath", e)
                false
            }
        }
    }
    
    /**
     * Get a presigned URL for the file
     */
    fun getPresignedUrl(filePath: String, duration: Duration = Duration.ofHours(1)): String {
        if (filePath.isBlank()) return ""
        
        return try {
            if (!fileExists(filePath)) {
                return ""
            }
            
            val presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest { it.bucket(bucketName).key(filePath) }
                .build()
                
            val presignedRequest = presigner.presignGetObject(presignRequest)
            presignedRequest.url().toString()
        } catch (ex: Exception) {
            logger.error("Failed to generate presigned URL for: $filePath", ex)
            ""
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