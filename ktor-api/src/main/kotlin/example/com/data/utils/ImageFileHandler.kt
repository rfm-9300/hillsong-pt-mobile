import example.com.web.utils.Strings
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.imageio.ImageIO
import example.com.data.db.image.ImageHashRepository
import example.com.data.db.image.ImageHashRepositoryImpl

object ImageFileHandler {
    private const val UPLOAD_DIR = "${Strings.RESOURCES_DIR}/uploads/images"
    private const val MAX_FILE_SIZE = 3 * 1024 * 1024 // 5MB
    private const val MIN_FILE_SIZE = 1024 // 1KB
    private const val MAX_IMAGE_WIDTH = 5000 // pixels
    private const val MAX_IMAGE_HEIGHT = 5000 // pixels
    private const val MIN_IMAGE_WIDTH = 50 // pixels
    private const val MIN_IMAGE_HEIGHT = 50 // pixels

    private var imageHashRepository: ImageHashRepository = ImageHashRepositoryImpl()


    suspend fun saveImage(fileBytes: ByteArray, originalFileName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Check file size constraints
                validateFileSize(fileBytes)

                // Validate image dimensions
                validateImageDimensions(fileBytes)

                // Calculate hash of the image content
                val imageHash = fileBytes.contentHashCode()

                // Check if we already have this image
                val existingImage = imageHashRepository.findByHash(imageHash)
                if (existingImage != null) {
                    return@withContext existingImage.imagePath
                }

                // Generate unique filename while preserving original extension
                val extension = originalFileName.substringAfterLast('.', "").lowercase()

                // Validate extension is an image type
                validateImageExtension(extension)

                val fileName = "${UUID.randomUUID()}.$extension"
                val file = File("$UPLOAD_DIR/$fileName")

                // Write the file
                file.writeBytes(fileBytes)

                // Verify the file was saved correctly
                if (!file.exists()) {
                    throw ImageSaveException("File was not created")
                }

                // Verify file size matches expected size
                if (file.length() != fileBytes.size.toLong()) {
                    file.delete() // Clean up corrupted file
                    throw ImageSaveException("File size mismatch: expected ${fileBytes.size} bytes but got ${file.length()} bytes")
                }

                // Save the hash to database
                imageHashRepository.save(fileName, imageHash)

                fileName // Return the generated filename
            } catch (e: ImageSaveException) {
                throw e
            } catch (e: Exception) {
                throw ImageSaveException("Failed to save image: ${e.message}")
            }
        }
    }

    private fun validateFileSize(fileBytes: ByteArray) {
        when {
            fileBytes.isEmpty() -> throw ImageSaveException("Image file is empty")
            fileBytes.size > MAX_FILE_SIZE -> throw ImageSaveException("Image file size (${fileBytes.size / 1024} KB) exceeds maximum allowed size (${MAX_FILE_SIZE / 1024} KB)")
            fileBytes.size < MIN_FILE_SIZE -> throw ImageSaveException("Image file size (${fileBytes.size / 1024} KB) is below minimum required size (${MIN_FILE_SIZE / 1024} KB)")
        }
    }

    private fun validateImageExtension(extension: String) {
        val validExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        if (extension !in validExtensions) {
            throw ImageSaveException("Invalid image file extension: .$extension. Allowed extensions are: ${validExtensions.joinToString(", ") { ".$it" }}")
        }
    }

    private fun validateImageDimensions(fileBytes: ByteArray) {
        try {
            val inputStream = fileBytes.inputStream()
            val bufferedImage = ImageIO.read(inputStream)

            if (bufferedImage == null) {
                throw ImageSaveException("Could not read image data - file may be corrupted or not a valid image")
            }

            val width = bufferedImage.width
            val height = bufferedImage.height

            when {
                width > MAX_IMAGE_WIDTH -> throw ImageSaveException("Image width ($width px) exceeds maximum allowed width ($MAX_IMAGE_WIDTH px)")
                height > MAX_IMAGE_HEIGHT -> throw ImageSaveException("Image height ($height px) exceeds maximum allowed height ($MAX_IMAGE_HEIGHT px)")
                width < MIN_IMAGE_WIDTH -> throw ImageSaveException("Image width ($width px) is below minimum required width ($MIN_IMAGE_WIDTH px)")
                height < MIN_IMAGE_HEIGHT -> throw ImageSaveException("Image height ($height px) is below minimum required height ($MIN_IMAGE_HEIGHT px)")
            }
        } catch (e: Exception) {
            if (e is ImageSaveException) throw e
            throw ImageSaveException("Failed to validate image dimensions: ${e.message}")
        }
    }

    class ImageSaveException(message: String) : Exception(message)
}