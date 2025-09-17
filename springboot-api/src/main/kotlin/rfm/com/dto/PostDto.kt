package rfm.com.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Request DTO for creating a new post
 */
data class CreatePostRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String,
    
    @field:NotBlank(message = "Content is required")
    @field:Size(max = 10000, message = "Content must not exceed 10000 characters")
    val content: String
)

/**
 * Request DTO for updating an existing post
 */
data class UpdatePostRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String,
    
    @field:NotBlank(message = "Content is required")
    @field:Size(max = 10000, message = "Content must not exceed 10000 characters")
    val content: String
)

/**
 * Request DTO for creating a comment on a post
 */
data class CreateCommentRequest(
    @field:NotBlank(message = "Comment content is required")
    @field:Size(max = 1000, message = "Comment must not exceed 1000 characters")
    val content: String
)

/**
 * Response DTO for post information
 */
data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val date: LocalDateTime,
    val headerImagePath: String?,
    val author: AuthorResponse,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByCurrentUser: Boolean = false
)

/**
 * Response DTO for post comment information
 */
data class CommentResponse(
    val id: Long,
    val content: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val date: LocalDateTime,
    val author: AuthorResponse
)

/**
 * Response DTO for author information in posts and comments
 */
data class AuthorResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val imagePath: String?
)

/**
 * Response DTO for paginated post results
 */
data class PostPageResponse(
    val posts: List<PostResponse>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

/**
 * Response DTO for paginated comment results
 */
data class CommentPageResponse(
    val comments: List<CommentResponse>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

/**
 * Response DTO for post statistics
 */
data class PostStatsResponse(
    val totalPosts: Long,
    val totalLikes: Long,
    val totalComments: Long,
    val mostLikedPost: PostResponse?,
    val mostCommentedPost: PostResponse?
)

/**
 * Request DTO for post search and filtering
 */
data class PostSearchRequest(
    val title: String? = null,
    val content: String? = null,
    val authorId: Long? = null,
    val fromDate: LocalDateTime? = null,
    val toDate: LocalDateTime? = null,
    val minLikes: Int? = null,
    val minComments: Int? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "date",
    val sortDirection: String = "desc"
)