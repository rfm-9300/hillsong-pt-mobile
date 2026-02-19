package rfm.com.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
class S3Config {

    @Value("\${app.minio.endpoint}")
    private lateinit var endpoint: String

    @Value("\${app.minio.access-key}")
    private lateinit var accessKey: String

    @Value("\${app.minio.secret-key}")
    private lateinit var secretKey: String

    @Bean
    fun s3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)
        
        return S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.US_EAST_1) // MinIO requires a region, usually us-east-1
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true) // Required for MinIO
                    .build()
            )
            .build()
    }
}
