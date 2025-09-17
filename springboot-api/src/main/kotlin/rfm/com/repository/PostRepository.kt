package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.Post
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import java.time.LocalDateTime

@Repository
interface PostRepository : JpaRepository<Post, Long> {
    
    /**
     * Find post by ID with author eagerly loaded
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE p.id = :id")
    fun findByIdWithAuthor(@Param("id") id: Long): Post?
    
    /**
     * Find post by ID with likes eagerly loaded
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.likedByUsers WHERE p.id = :id")
    fun findByIdWithLikes(@Param("id") id: Long): Post?
    
    /**
     * Find post by ID with comments eagerly loaded
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.id = :id")
    fun findByIdWithComments(@Param("id") id: Long): Post?
    
    /**
     * Find post by ID with all relationships eagerly loaded
     */
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.likedByUsers " +
           "LEFT JOIN FETCH p.comments " +
           "WHERE p.id = :id")
    fun findByIdWithAllRelationships(@Param("id") id: Long): Post?
    
    /**
     * Find all posts with pagination and sorting (default order by date desc)
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author ORDER BY p.date DESC")
    fun findAllWithAuthor(pageable: Pageable): Page<Post>
    
    /**
     * Find posts by author
     */
    @Query("SELECT p FROM Post p WHERE p.author = :author ORDER BY p.date DESC")
    fun findByAuthor(@Param("author") author: UserProfile): List<Post>
    
    /**
     * Find posts by author with pagination
     */
    @Query("SELECT p FROM Post p WHERE p.author = :author ORDER BY p.date DESC")
    fun findByAuthor(@Param("author") author: UserProfile, pageable: Pageable): Page<Post>
    
    /**
     * Find posts liked by a specific user
     */
    @Query("SELECT p FROM Post p JOIN p.likedByUsers l WHERE l = :user ORDER BY p.date DESC")
    fun findPostsLikedByUser(@Param("user") user: User): List<Post>
    
    /**
     * Find posts liked by a specific user with pagination
     */
    @Query("SELECT p FROM Post p JOIN p.likedByUsers l WHERE l = :user ORDER BY p.date DESC")
    fun findPostsLikedByUser(@Param("user") user: User, pageable: Pageable): Page<Post>
    
    /**
     * Find posts by title (case-insensitive search)
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY p.date DESC")
    fun findByTitleContainingIgnoreCase(@Param("title") title: String): List<Post>
    
    /**
     * Find posts by title with pagination
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY p.date DESC")
    fun findByTitleContainingIgnoreCase(@Param("title") title: String, pageable: Pageable): Page<Post>
    
    /**
     * Find posts by content (case-insensitive search)
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%')) ORDER BY p.date DESC")
    fun findByContentContainingIgnoreCase(@Param("content") content: String): List<Post>
    
    /**
     * Find posts by content with pagination
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%')) ORDER BY p.date DESC")
    fun findByContentContainingIgnoreCase(@Param("content") content: String, pageable: Pageable): Page<Post>
    
    /**
     * Find posts by date range
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE p.date BETWEEN :startDate AND :endDate ORDER BY p.date DESC")
    fun findPostsByDateRange(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Post>
    
    /**
     * Find posts by date range with pagination
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE p.date BETWEEN :startDate AND :endDate ORDER BY p.date DESC")
    fun findPostsByDateRange(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime, pageable: Pageable): Page<Post>
    
    /**
     * Find most liked posts (ordered by like count)
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author ORDER BY SIZE(p.likedByUsers) DESC")
    fun findMostLikedPosts(pageable: Pageable): Page<Post>
    
    /**
     * Find most commented posts (ordered by comment count)
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author ORDER BY SIZE(p.comments) DESC")
    fun findMostCommentedPosts(pageable: Pageable): Page<Post>
    
    /**
     * Find recent posts (posts from the last N days)
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE p.date >= :fromDate ORDER BY p.date DESC")
    fun findRecentPosts(@Param("fromDate") fromDate: LocalDateTime): List<Post>
    
    /**
     * Find recent posts with pagination
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE p.date >= :fromDate ORDER BY p.date DESC")
    fun findRecentPosts(@Param("fromDate") fromDate: LocalDateTime, pageable: Pageable): Page<Post>
    
    /**
     * Count posts by author
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author = :author")
    fun countPostsByAuthor(@Param("author") author: UserProfile): Long
    
    /**
     * Count posts liked by a user
     */
    @Query("SELECT COUNT(p) FROM Post p JOIN p.likedByUsers l WHERE l = :user")
    fun countPostsLikedByUser(@Param("user") user: User): Long
    
    /**
     * Count posts created after a specific date
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.date >= :fromDate")
    fun countPostsCreatedAfter(@Param("fromDate") fromDate: LocalDateTime): Long
    
    /**
     * Find posts with minimum like count
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE SIZE(p.likedByUsers) >= :minLikes ORDER BY p.date DESC")
    fun findPostsWithMinimumLikes(@Param("minLikes") minLikes: Int): List<Post>
    
    /**
     * Find posts with minimum comment count
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE SIZE(p.comments) >= :minComments ORDER BY p.date DESC")
    fun findPostsWithMinimumComments(@Param("minComments") minComments: Int): List<Post>
}