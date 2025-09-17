package rfm.com.repository

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import rfm.com.entity.*
import java.time.LocalDateTime

class PostRepositoryTest : BaseRepositoryTest() {
    
    @Autowired
    private lateinit var postRepository: PostRepository
    
    @Autowired
    private lateinit var entityManager: TestEntityManager
    
    private lateinit var author: UserProfile
    private lateinit var likerUser: User
    private lateinit var commenterUser: User
    private lateinit var testPost: Post
    private lateinit var popularPost: Post
    
    @BeforeEach
    fun setUp() {
        // Create author user and profile
        val authorUser = User(
            email = "author@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        val savedAuthorUser = entityManager.persistAndFlush(authorUser)
        
        author = UserProfile(
            user = savedAuthorUser,
            firstName = "Post",
            lastName = "Author",
            email = "author@example.com",
            phone = "1234567890",
            isAdmin = false
        )
        entityManager.persistAndFlush(author)
        
        // Create liker and commenter users
        likerUser = User(
            email = "liker@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        entityManager.persistAndFlush(likerUser)
        
        commenterUser = User(
            email = "commenter@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        entityManager.persistAndFlush(commenterUser)
        
        // Create test posts
        testPost = Post(
            title = "Test Post",
            content = "This is a test post content",
            author = author,
            headerImagePath = "/images/test.jpg"
        )
        entityManager.persistAndFlush(testPost)
        
        popularPost = Post(
            title = "Popular Post",
            content = "This is a popular post with likes and comments",
            author = author,
            headerImagePath = "/images/popular.jpg"
        )
        val savedPopularPost = entityManager.persistAndFlush(popularPost)
        
        // Add likes to popular post
        savedPopularPost.likedByUsers.add(likerUser)
        savedPopularPost.likedByUsers.add(commenterUser)
        entityManager.persistAndFlush(savedPopularPost)
        
        // Create comments for popular post
        val comment1 = PostComment(
            content = "Great post!",
            author = commenterUser,
            post = savedPopularPost
        )
        val comment2 = PostComment(
            content = "Very informative",
            author = likerUser,
            post = savedPopularPost
        )
        entityManager.persistAndFlush(comment1)
        entityManager.persistAndFlush(comment2)
        
        entityManager.clear()
    }
    
    @Test
    fun `findByIdWithAuthor should eagerly load author`() {
        // When
        val foundPost = postRepository.findByIdWithAuthor(testPost.id!!)
        
        // Then
        assertNotNull(foundPost)
        assertNotNull(foundPost?.author)
        assertEquals("Post", foundPost?.author?.firstName)
        assertEquals("Author", foundPost?.author?.lastName)
    }
    
    @Test
    fun `findByIdWithLikes should eagerly load liked by users`() {
        // When
        val foundPost = postRepository.findByIdWithLikes(popularPost.id!!)
        
        // Then
        assertNotNull(foundPost)
        assertEquals(2, foundPost?.likedByUsers?.size)
        assertTrue(foundPost?.likedByUsers?.any { it.email == "liker@example.com" } == true)
        assertTrue(foundPost?.likedByUsers?.any { it.email == "commenter@example.com" } == true)
    }
    
    @Test
    fun `findByIdWithComments should eagerly load comments`() {
        // When
        val foundPost = postRepository.findByIdWithComments(popularPost.id!!)
        
        // Then
        assertNotNull(foundPost)
        assertEquals(2, foundPost?.comments?.size)
        assertTrue(foundPost?.comments?.any { it.content == "Great post!" } == true)
        assertTrue(foundPost?.comments?.any { it.content == "Very informative" } == true)
    }
    
    @Test
    fun `findByIdWithAllRelationships should eagerly load all relationships`() {
        // When
        val foundPost = postRepository.findByIdWithAllRelationships(popularPost.id!!)
        
        // Then
        assertNotNull(foundPost)
        assertNotNull(foundPost?.author)
        assertEquals(2, foundPost?.likedByUsers?.size)
        assertEquals(2, foundPost?.comments?.size)
        assertEquals("Post", foundPost?.author?.firstName)
    }
    
    @Test
    fun `findAllWithAuthor should return paginated posts with authors`() {
        // When
        val page = postRepository.findAllWithAuthor(PageRequest.of(0, 10))
        
        // Then
        assertTrue(page.content.isNotEmpty())
        assertEquals(2, page.totalElements)
        assertTrue(page.content.all { it.author != null })
        assertTrue(page.content.any { it.title == "Test Post" })
        assertTrue(page.content.any { it.title == "Popular Post" })
    }
    
    @Test
    fun `findByAuthor should return posts by specific author`() {
        // When
        val authorPosts = postRepository.findByAuthor(author)
        
        // Then
        assertEquals(2, authorPosts.size)
        assertTrue(authorPosts.all { it.author.id == author.id })
        assertTrue(authorPosts.any { it.title == "Test Post" })
        assertTrue(authorPosts.any { it.title == "Popular Post" })
    }
    
    @Test
    fun `findByAuthor with pagination should return paginated results`() {
        // When
        val page = postRepository.findByAuthor(author, PageRequest.of(0, 1))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(2, page.totalElements)
        assertEquals(2, page.totalPages)
        assertTrue(page.content.all { it.author.id == author.id })
    }
    
    @Test
    fun `findPostsLikedByUser should return posts liked by specific user`() {
        // When
        val likedPosts = postRepository.findPostsLikedByUser(likerUser)
        
        // Then
        assertTrue(likedPosts.isNotEmpty())
        assertTrue(likedPosts.any { it.title == "Popular Post" })
        assertTrue(likedPosts.all { post -> 
            post.likedByUsers.any { it.id == likerUser.id }
        })
    }
    
    @Test
    fun `findPostsLikedByUser with pagination should return paginated results`() {
        // When
        val page = postRepository.findPostsLikedByUser(likerUser, PageRequest.of(0, 10))
        
        // Then
        assertTrue(page.content.isNotEmpty())
        assertTrue(page.content.any { it.title == "Popular Post" })
    }
    
    @Test
    fun `findByTitleContainingIgnoreCase should find posts by title`() {
        // When
        val testPosts = postRepository.findByTitleContainingIgnoreCase("test")
        val popularPosts = postRepository.findByTitleContainingIgnoreCase("POPULAR")
        
        // Then
        assertTrue(testPosts.isNotEmpty())
        assertTrue(testPosts.any { it.title == "Test Post" })
        
        assertTrue(popularPosts.isNotEmpty())
        assertTrue(popularPosts.any { it.title == "Popular Post" })
    }
    
    @Test
    fun `findByContentContainingIgnoreCase should find posts by content`() {
        // When
        val testPosts = postRepository.findByContentContainingIgnoreCase("test post content")
        val popularPosts = postRepository.findByContentContainingIgnoreCase("POPULAR")
        
        // Then
        assertTrue(testPosts.isNotEmpty())
        assertTrue(testPosts.any { it.title == "Test Post" })
        
        assertTrue(popularPosts.isNotEmpty())
        assertTrue(popularPosts.any { it.title == "Popular Post" })
    }
    
    @Test
    fun `findPostsByDateRange should return posts within date range`() {
        // Given
        val startDate = LocalDateTime.now().minusHours(1)
        val endDate = LocalDateTime.now().plusHours(1)
        
        // When
        val postsInRange = postRepository.findPostsByDateRange(startDate, endDate)
        
        // Then
        assertTrue(postsInRange.isNotEmpty())
        assertTrue(postsInRange.all { it.date.isAfter(startDate) && it.date.isBefore(endDate) })
        assertEquals(2, postsInRange.size)
    }
    
    @Test
    fun `findPostsByDateRange with pagination should return paginated results`() {
        // Given
        val startDate = LocalDateTime.now().minusHours(1)
        val endDate = LocalDateTime.now().plusHours(1)
        
        // When
        val page = postRepository.findPostsByDateRange(startDate, endDate, PageRequest.of(0, 1))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(2, page.totalElements)
    }
    
    @Test
    fun `findMostLikedPosts should return posts ordered by like count`() {
        // When
        val page = postRepository.findMostLikedPosts(PageRequest.of(0, 10))
        
        // Then
        assertTrue(page.content.isNotEmpty())
        // Popular post should come first (has 2 likes vs 0 likes)
        assertEquals("Popular Post", page.content.first().title)
    }
    
    @Test
    fun `findMostCommentedPosts should return posts ordered by comment count`() {
        // When
        val page = postRepository.findMostCommentedPosts(PageRequest.of(0, 10))
        
        // Then
        assertTrue(page.content.isNotEmpty())
        // Popular post should come first (has 2 comments vs 0 comments)
        assertEquals("Popular Post", page.content.first().title)
    }
    
    @Test
    fun `findRecentPosts should return posts from specified date`() {
        // Given
        val fromDate = LocalDateTime.now().minusHours(1)
        
        // When
        val recentPosts = postRepository.findRecentPosts(fromDate)
        
        // Then
        assertTrue(recentPosts.isNotEmpty())
        assertTrue(recentPosts.all { it.date.isAfter(fromDate) })
        assertEquals(2, recentPosts.size)
    }
    
    @Test
    fun `findRecentPosts with pagination should return paginated results`() {
        // Given
        val fromDate = LocalDateTime.now().minusHours(1)
        
        // When
        val page = postRepository.findRecentPosts(fromDate, PageRequest.of(0, 1))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(2, page.totalElements)
    }
    
    @Test
    fun `countPostsByAuthor should return correct count`() {
        // When
        val postCount = postRepository.countPostsByAuthor(author)
        
        // Then
        assertEquals(2, postCount)
    }
    
    @Test
    fun `countPostsLikedByUser should return correct count`() {
        // When
        val likedCount = postRepository.countPostsLikedByUser(likerUser)
        
        // Then
        assertEquals(1, likedCount) // Only liked the popular post
    }
    
    @Test
    fun `countPostsCreatedAfter should return correct count`() {
        // Given
        val fromDate = LocalDateTime.now().minusHours(1)
        
        // When
        val recentCount = postRepository.countPostsCreatedAfter(fromDate)
        
        // Then
        assertEquals(2, recentCount)
    }
    
    @Test
    fun `findPostsWithMinimumLikes should return posts with at least specified likes`() {
        // When
        val postsWithLikes = postRepository.findPostsWithMinimumLikes(1)
        val postsWithManyLikes = postRepository.findPostsWithMinimumLikes(3)
        
        // Then
        assertTrue(postsWithLikes.isNotEmpty())
        assertTrue(postsWithLikes.any { it.title == "Popular Post" })
        assertFalse(postsWithLikes.any { it.title == "Test Post" })
        
        assertTrue(postsWithManyLikes.isEmpty()) // No posts have 3+ likes
    }
    
    @Test
    fun `findPostsWithMinimumComments should return posts with at least specified comments`() {
        // When
        val postsWithComments = postRepository.findPostsWithMinimumComments(1)
        val postsWithManyComments = postRepository.findPostsWithMinimumComments(3)
        
        // Then
        assertTrue(postsWithComments.isNotEmpty())
        assertTrue(postsWithComments.any { it.title == "Popular Post" })
        assertFalse(postsWithComments.any { it.title == "Test Post" })
        
        assertTrue(postsWithManyComments.isEmpty()) // No posts have 3+ comments
    }
    
    @Test
    fun `save should persist post with all fields`() {
        // Given
        val newPost = Post(
            title = "New Test Post",
            content = "This is a new test post",
            author = author,
            headerImagePath = "/images/new.jpg"
        )
        
        // When
        val savedPost = postRepository.save(newPost)
        entityManager.flush()
        entityManager.clear()
        
        // Then
        assertNotNull(savedPost.id)
        
        val foundPost = postRepository.findById(savedPost.id!!)
        assertTrue(foundPost.isPresent)
        assertEquals("New Test Post", foundPost.get().title)
        assertEquals("This is a new test post", foundPost.get().content)
        assertEquals("/images/new.jpg", foundPost.get().headerImagePath)
    }
    
    @Test
    fun `delete should remove post and cascade to comments`() {
        // Given
        val postToDelete = postRepository.findByIdWithComments(popularPost.id!!)
        assertNotNull(postToDelete)
        assertEquals(2, postToDelete?.comments?.size)
        val postId = postToDelete!!.id!!
        
        // When
        postRepository.delete(postToDelete)
        entityManager.flush()
        entityManager.clear()
        
        // Then
        val deletedPost = postRepository.findById(postId)
        assertFalse(deletedPost.isPresent)
    }
    
    @Test
    fun `post entity should handle equals and hashCode correctly`() {
        // Given
        val post1 = Post(
            id = 1L,
            title = "Post 1",
            content = "Content 1",
            author = author
        )
        val post2 = Post(
            id = 1L,
            title = "Post 2",
            content = "Content 2",
            author = author
        )
        val post3 = Post(
            id = 2L,
            title = "Post 1",
            content = "Content 1",
            author = author
        )
        
        // Then
        assertEquals(post1, post2) // Same ID
        assertNotEquals(post1, post3) // Different ID
        assertEquals(post1.hashCode(), post2.hashCode()) // Same ID should have same hash
    }
}