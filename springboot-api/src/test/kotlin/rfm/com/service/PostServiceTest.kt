package rfm.com.service

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.CreateCommentRequest
import rfm.com.dto.CreatePostRequest
import rfm.com.entity.*
import rfm.com.repository.PostCommentRepository
import rfm.com.repository.PostRepository
import rfm.com.repository.UserProfileRepository
import rfm.com.repository.UserRepository
import java.time.LocalDateTime
import java.util.*

class PostServiceTest {

    private val postRepository = mockk<PostRepository>()
    private val postCommentRepository = mockk<PostCommentRepository>()
    private val userRepository = mockk<UserRepository>()
    private val userProfileRepository = mockk<UserProfileRepository>()
    private val fileStorageService = mockk<FileStorageService>()

    private lateinit var postService: PostService

    private lateinit var testUser: User
    private lateinit var testUserProfile: UserProfile
    private lateinit var testPost: Post
    private lateinit var testComment: PostComment

    @BeforeEach
    fun setUp() {
        postService = PostService(
            postRepository,
            postCommentRepository,
            userRepository,
            userProfileRepository,
            fileStorageService
        )

        // Create test entities
        testUser = User(
            id = 1L,
            email = "test@example.com",
            password = "hashedPassword",
            salt = "salt",
            verified = true,
            authProvider = AuthProvider.LOCAL
        )

        testUserProfile = UserProfile(
            id = 1L,
            user = testUser,
            firstName = "John",
            lastName = "Doe",
            email = "test@example.com",
            phone = "1234567890",
            imagePath = "profile.jpg",
            isAdmin = false
        )

        testPost = Post(
            id = 1L,
            title = "Test Post",
            content = "This is a test post content",
            author = testUserProfile,
            headerImagePath = "test-image.jpg",
            date = LocalDateTime.now()
        )

        testComment = PostComment(
            id = 1L,
            post = testPost,
            user = testUser,
            content = "This is a test comment",
            date = LocalDateTime.now()
        )
    }

    @Test
    fun `createPost should create post successfully`() {
        // Given
        val createPostRequest = CreatePostRequest(
            title = "New Post",
            content = "New post content"
        )
        val headerImage = mockk<MultipartFile>()
        val authorId = 1L

        every { userProfileRepository.findById(authorId) } returns Optional.of(testUserProfile)
        every { headerImage.isEmpty } returns false
        every { fileStorageService.storePostImage(headerImage) } returns "new-image.jpg"
        every { postRepository.save(any<Post>()) } returns testPost.copy(
            title = createPostRequest.title,
            content = createPostRequest.content,
            headerImagePath = "new-image.jpg"
        )

        // When
        val result = postService.createPost(createPostRequest, authorId, headerImage)

        // Then
        assertEquals(createPostRequest.title, result.title)
        assertEquals(createPostRequest.content, result.content)
        assertEquals("new-image.jpg", result.headerImagePath)
        verify { postRepository.save(any<Post>()) }
    }

    @Test
    fun `createPost should throw exception when author not found`() {
        // Given
        val createPostRequest = CreatePostRequest(
            title = "New Post",
            content = "New post content"
        )
        val authorId = 999L

        every { userProfileRepository.findById(authorId) } returns Optional.empty()

        // When & Then
        assertThrows<IllegalArgumentException> {
            postService.createPost(createPostRequest, authorId, null)
        }
    }

    @Test
    fun `getAllPosts should return paginated posts`() {
        // Given
        val page = 0
        val size = 20
        val pageable = PageRequest.of(page, size, Sort.by("date").descending())
        val postsPage = PageImpl(listOf(testPost), pageable, 1)

        every { postRepository.findAllWithAuthor(pageable) } returns postsPage
        every { userRepository.findById(any()) } returns Optional.of(testUser)

        // When
        val result = postService.getAllPosts(page, size, "date", "desc", 1L)

        // Then
        assertEquals(1, result.posts.size)
        assertEquals(testPost.title, result.posts[0].title)
        assertEquals(0, result.currentPage)
        assertEquals(1, result.totalPages)
        assertEquals(1L, result.totalElements)
    }

    @Test
    fun `getPostById should return post when found`() {
        // Given
        val postId = 1L
        val currentUserId = 1L

        every { postRepository.findByIdWithAllRelationships(postId) } returns testPost
        every { userRepository.findById(currentUserId) } returns Optional.of(testUser)

        // When
        val result = postService.getPostById(postId, currentUserId)

        // Then
        assertEquals(testPost.title, result.title)
        assertEquals(testPost.content, result.content)
        assertEquals(testPost.id, result.id)
    }

    @Test
    fun `getPostById should throw exception when post not found`() {
        // Given
        val postId = 999L

        every { postRepository.findByIdWithAllRelationships(postId) } returns null

        // When & Then
        assertThrows<IllegalArgumentException> {
            postService.getPostById(postId)
        }
    }

    @Test
    fun `togglePostLike should add like when not liked`() {
        // Given
        val postId = 1L
        val userId = 1L
        val postWithoutLike = testPost.copy(likedByUsers = mutableSetOf())

        every { postRepository.findByIdWithLikes(postId) } returns postWithoutLike
        every { userRepository.findById(userId) } returns Optional.of(testUser)
        every { postRepository.save(any<Post>()) } returns postWithoutLike.apply { 
            likedByUsers.add(testUser) 
        }

        // When
        val result = postService.togglePostLike(postId, userId)

        // Then
        assertTrue(result.isLikedByCurrentUser)
        verify { postRepository.save(any<Post>()) }
    }

    @Test
    fun `togglePostLike should remove like when already liked`() {
        // Given
        val postId = 1L
        val userId = 1L
        val postWithLike = testPost.copy(likedByUsers = mutableSetOf(testUser))

        every { postRepository.findByIdWithLikes(postId) } returns postWithLike
        every { userRepository.findById(userId) } returns Optional.of(testUser)
        every { postRepository.save(any<Post>()) } returns postWithLike.apply { 
            likedByUsers.remove(testUser) 
        }

        // When
        val result = postService.togglePostLike(postId, userId)

        // Then
        assertFalse(result.isLikedByCurrentUser)
        verify { postRepository.save(any<Post>()) }
    }

    @Test
    fun `addComment should create comment successfully`() {
        // Given
        val postId = 1L
        val userId = 1L
        val createCommentRequest = CreateCommentRequest("This is a new comment")

        every { postRepository.findById(postId) } returns Optional.of(testPost)
        every { userRepository.findById(userId) } returns Optional.of(testUser)
        every { postCommentRepository.save(any<PostComment>()) } returns testComment.copy(
            content = createCommentRequest.content
        )

        // When
        val result = postService.addComment(postId, createCommentRequest, userId)

        // Then
        assertEquals(createCommentRequest.content, result.content)
        verify { postCommentRepository.save(any<PostComment>()) }
    }

    @Test
    fun `addComment should throw exception when post not found`() {
        // Given
        val postId = 999L
        val userId = 1L
        val createCommentRequest = CreateCommentRequest("This is a new comment")

        every { postRepository.findById(postId) } returns Optional.empty()

        // When & Then
        assertThrows<IllegalArgumentException> {
            postService.addComment(postId, createCommentRequest, userId)
        }
    }

    @Test
    fun `deletePost should delete post when user is author`() {
        // Given
        val postId = 1L
        val userId = 1L

        every { postRepository.findByIdWithAuthor(postId) } returns testPost
        every { userProfileRepository.findById(userId) } returns Optional.of(testUserProfile)
        every { fileStorageService.deleteFile(any()) } returns true
        every { postRepository.delete(testPost) } just Runs

        // When
        val result = postService.deletePost(postId, userId)

        // Then
        assertTrue(result)
        verify { postRepository.delete(testPost) }
    }

    @Test
    fun `deletePost should throw exception when user is not authorized`() {
        // Given
        val postId = 1L
        val userId = 2L // Different user
        val otherUserProfile = testUserProfile.copy(id = 2L, isAdmin = false)

        every { postRepository.findByIdWithAuthor(postId) } returns testPost
        every { userProfileRepository.findById(userId) } returns Optional.of(otherUserProfile)

        // When & Then
        assertThrows<SecurityException> {
            postService.deletePost(postId, userId)
        }
    }

    @Test
    fun `deleteComment should delete comment when user is author`() {
        // Given
        val commentId = 1L
        val userId = 1L

        every { postCommentRepository.findByIdWithUser(commentId) } returns testComment
        every { userProfileRepository.findById(userId) } returns Optional.of(testUserProfile)
        every { postCommentRepository.delete(testComment) } just Runs

        // When
        val result = postService.deleteComment(commentId, userId)

        // Then
        assertTrue(result)
        verify { postCommentRepository.delete(testComment) }
    }

    @Test
    fun `getPostStats should return correct statistics`() {
        // Given
        val totalPosts = 10L
        val mostLikedPostPage = PageImpl(listOf(testPost))
        val mostCommentedPostPage = PageImpl(listOf(testPost))
        val allPosts = listOf(
            testPost.copy(likedByUsers = mutableSetOf(testUser)),
            testPost.copy(comments = mutableSetOf(testComment))
        )

        every { postRepository.count() } returns totalPosts
        every { postRepository.findMostLikedPosts(any()) } returns mostLikedPostPage
        every { postRepository.findMostCommentedPosts(any()) } returns mostCommentedPostPage
        every { postRepository.findAll() } returns allPosts

        // When
        val result = postService.getPostStats()

        // Then
        assertEquals(totalPosts, result.totalPosts)
        assertEquals(1L, result.totalLikes)
        assertEquals(1L, result.totalComments)
    }
}