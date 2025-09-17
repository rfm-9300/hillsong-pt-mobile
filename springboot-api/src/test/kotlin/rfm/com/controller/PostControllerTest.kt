package rfm.com.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rfm.com.dto.*
import rfm.com.service.PostService
import java.time.LocalDateTime

@WebMvcTest(PostController::class)
class PostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var postService: PostService

    @Test
    @WithMockUser(username = "1")
    fun `getAllPosts should return posts successfully`() {
        // Given
        val postResponse = PostResponse(
            id = 1L,
            title = "Test Post",
            content = "Test content",
            date = LocalDateTime.now(),
            headerImagePath = "test.jpg",
            author = AuthorResponse(1L, "John Doe", "john@example.com", "profile.jpg"),
            likeCount = 5,
            commentCount = 3,
            isLikedByCurrentUser = false
        )

        val pageResponse = PostPageResponse(
            posts = listOf(postResponse),
            currentPage = 0,
            totalPages = 1,
            totalElements = 1L,
            hasNext = false,
            hasPrevious = false
        )

        every { postService.getAllPosts(any(), any(), any(), any(), any()) } returns pageResponse

        // When & Then
        mockMvc.perform(get("/api/posts")
            .param("page", "0")
            .param("size", "20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.posts").isArray)
            .andExpect(jsonPath("$.data.posts[0].title").value("Test Post"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `getPostById should return post successfully`() {
        // Given
        val postResponse = PostResponse(
            id = 1L,
            title = "Test Post",
            content = "Test content",
            date = LocalDateTime.now(),
            headerImagePath = "test.jpg",
            author = AuthorResponse(1L, "John Doe", "john@example.com", "profile.jpg"),
            likeCount = 5,
            commentCount = 3,
            isLikedByCurrentUser = false
        )

        every { postService.getPostById(1L, 1L) } returns postResponse

        // When & Then
        mockMvc.perform(get("/api/posts/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("Test Post"))
            .andExpect(jsonPath("$.data.id").value(1))
    }

    @Test
    @WithMockUser(username = "1")
    fun `addComment should create comment successfully`() {
        // Given
        val createCommentRequest = CreateCommentRequest("This is a test comment")
        val commentResponse = CommentResponse(
            id = 1L,
            content = "This is a test comment",
            date = LocalDateTime.now(),
            author = AuthorResponse(1L, "John Doe", "john@example.com", "profile.jpg")
        )

        every { postService.addComment(1L, createCommentRequest, 1L) } returns commentResponse

        // When & Then
        mockMvc.perform(post("/api/posts/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createCommentRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").value("This is a test comment"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `togglePostLike should toggle like successfully`() {
        // Given
        val postResponse = PostResponse(
            id = 1L,
            title = "Test Post",
            content = "Test content",
            date = LocalDateTime.now(),
            headerImagePath = "test.jpg",
            author = AuthorResponse(1L, "John Doe", "john@example.com", "profile.jpg"),
            likeCount = 6,
            commentCount = 3,
            isLikedByCurrentUser = true
        )

        every { postService.togglePostLike(1L, 1L) } returns postResponse

        // When & Then
        mockMvc.perform(post("/api/posts/1/like"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.isLikedByCurrentUser").value(true))
            .andExpect(jsonPath("$.data.likeCount").value(6))
    }

    @Test
    @WithMockUser(username = "1")
    fun `getPostComments should return comments successfully`() {
        // Given
        val commentResponse = CommentResponse(
            id = 1L,
            content = "This is a test comment",
            date = LocalDateTime.now(),
            author = AuthorResponse(1L, "John Doe", "john@example.com", "profile.jpg")
        )

        val pageResponse = CommentPageResponse(
            comments = listOf(commentResponse),
            currentPage = 0,
            totalPages = 1,
            totalElements = 1L,
            hasNext = false,
            hasPrevious = false
        )

        every { postService.getPostComments(1L, 0, 20) } returns pageResponse

        // When & Then
        mockMvc.perform(get("/api/posts/1/comments")
            .param("page", "0")
            .param("size", "20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.comments").isArray)
            .andExpect(jsonPath("$.data.comments[0].content").value("This is a test comment"))
    }

    @Test
    fun `getAllPosts should work without authentication`() {
        // Given
        val postResponse = PostResponse(
            id = 1L,
            title = "Test Post",
            content = "Test content",
            date = LocalDateTime.now(),
            headerImagePath = "test.jpg",
            author = AuthorResponse(1L, "John Doe", "john@example.com", "profile.jpg"),
            likeCount = 5,
            commentCount = 3,
            isLikedByCurrentUser = false
        )

        val pageResponse = PostPageResponse(
            posts = listOf(postResponse),
            currentPage = 0,
            totalPages = 1,
            totalElements = 1L,
            hasNext = false,
            hasPrevious = false
        )

        every { postService.getAllPosts(any(), any(), any(), any(), null) } returns pageResponse

        // When & Then
        mockMvc.perform(get("/api/posts"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.posts").isArray)
    }

    @Test
    @WithMockUser(username = "1")
    fun `getPostStats should return statistics successfully`() {
        // Given
        val statsResponse = PostStatsResponse(
            totalPosts = 10L,
            totalLikes = 50L,
            totalComments = 30L,
            mostLikedPost = null,
            mostCommentedPost = null
        )

        every { postService.getPostStats() } returns statsResponse

        // When & Then
        mockMvc.perform(get("/api/posts/stats"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalPosts").value(10))
            .andExpect(jsonPath("$.data.totalLikes").value(50))
            .andExpect(jsonPath("$.data.totalComments").value(30))
    }

    @Test
    @WithMockUser(username = "1")
    fun `createPost should handle validation errors`() {
        // Given - invalid request with empty title
        val invalidRequest = CreatePostRequest(
            title = "",
            content = "Test content"
        )

        // When & Then
        mockMvc.perform(post("/api/posts")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .param("post", objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = "1")
    fun `updatePost should return 404 for non-existent post`() {
        // Given
        val updateRequest = UpdatePostRequest(
            title = "Updated Title",
            content = "Updated content"
        )

        every { postService.updatePost(999L, updateRequest, 1L, null) } throws IllegalArgumentException("Post not found")

        // When & Then
        mockMvc.perform(put("/api/posts/999")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .param("post", objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Post not found"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `deletePost should return success when post is deleted`() {
        // Given
        every { postService.deletePost(1L, 1L) } returns Unit

        // When & Then
        mockMvc.perform(delete("/api/posts/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Post deleted successfully"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `deletePost should return 403 for unauthorized user`() {
        // Given
        every { postService.deletePost(1L, 1L) } throws SecurityException("Not authorized to delete this post")

        // When & Then
        mockMvc.perform(delete("/api/posts/1"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Not authorized to delete this post"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `deleteComment should return success when comment is deleted`() {
        // Given
        every { postService.deleteComment(1L, 1L) } returns Unit

        // When & Then
        mockMvc.perform(delete("/api/posts/comments/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Comment deleted successfully"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `searchPosts should return filtered results`() {
        // Given
        val searchRequest = PostSearchRequest(
            query = "test",
            page = 0,
            size = 20
        )

        val postResponse = PostResponse(
            id = 1L,
            title = "Test Post",
            content = "Test content",
            date = LocalDateTime.now(),
            headerImagePath = "test.jpg",
            author = AuthorResponse(1L, "John Doe", "john@example.com", "profile.jpg"),
            likeCount = 5,
            commentCount = 3,
            isLikedByCurrentUser = false
        )

        val pageResponse = PostPageResponse(
            posts = listOf(postResponse),
            currentPage = 0,
            totalPages = 1,
            totalElements = 1L,
            hasNext = false,
            hasPrevious = false
        )

        every { postService.searchPosts(searchRequest, 1L) } returns pageResponse

        // When & Then
        mockMvc.perform(post("/api/posts/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(searchRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.posts").isArray)
            .andExpect(jsonPath("$.data.posts[0].title").value("Test Post"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `getPostsByAuthor should return author posts`() {
        // Given
        val postResponse = PostResponse(
            id = 1L,
            title = "Author Post",
            content = "Author content",
            date = LocalDateTime.now(),
            headerImagePath = "test.jpg",
            author = AuthorResponse(2L, "Jane Doe", "jane@example.com", "profile.jpg"),
            likeCount = 5,
            commentCount = 3,
            isLikedByCurrentUser = false
        )

        val pageResponse = PostPageResponse(
            posts = listOf(postResponse),
            currentPage = 0,
            totalPages = 1,
            totalElements = 1L,
            hasNext = false,
            hasPrevious = false
        )

        every { postService.getPostsByAuthor(2L, 0, 20, 1L) } returns pageResponse

        // When & Then
        mockMvc.perform(get("/api/posts/author/2")
            .param("page", "0")
            .param("size", "20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.posts").isArray)
            .andExpect(jsonPath("$.data.posts[0].author.id").value(2))
    }
}