package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.Post
import rfm.com.entity.PostComment
import rfm.com.entity.User
import java.time.LocalDateTime

@Repository
interface PostCommentRepository : JpaRepository<PostComment, Long> {
    
    /**
     * Find comment by ID with user eagerly loaded
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user WHERE pc.id = :id")
    fun findByIdWithUser(@Param("id") id: Long): PostComment?
    
    /**
     * Find comment by ID with post eagerly loaded
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.post WHERE pc.id = :id")
    fun findByIdWithPost(@Param("id") id: Long): PostComment?
    
    /**
     * Find comment by ID with all relationships eagerly loaded
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user LEFT JOIN FETCH pc.post WHERE pc.id = :id")
    fun findByIdWithAllRelationships(@Param("id") id: Long): PostComment?
    
    /**
     * Find all comments for a specific post
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user WHERE pc.post = :post ORDER BY pc.date ASC")
    fun findByPost(@Param("post") post: Post): List<PostComment>
    
    /**
     * Find all comments for a specific post with pagination
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user WHERE pc.post = :post ORDER BY pc.date ASC")
    fun findByPost(@Param("post") post: Post, pageable: Pageable): Page<PostComment>
    
    /**
     * Find all comments by a specific user
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.post WHERE pc.user = :user ORDER BY pc.date DESC")
    fun findByUser(@Param("user") user: User): List<PostComment>
    
    /**
     * Find all comments by a specific user with pagination
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.post WHERE pc.user = :user ORDER BY pc.date DESC")
    fun findByUser(@Param("user") user: User, pageable: Pageable): Page<PostComment>
    
    /**
     * Find comments by content (case-insensitive search)
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user LEFT JOIN FETCH pc.post WHERE LOWER(pc.content) LIKE LOWER(CONCAT('%', :content, '%')) ORDER BY pc.date DESC")
    fun findByContentContainingIgnoreCase(@Param("content") content: String): List<PostComment>
    
    /**
     * Find comments by content with pagination
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user LEFT JOIN FETCH pc.post WHERE LOWER(pc.content) LIKE LOWER(CONCAT('%', :content, '%')) ORDER BY pc.date DESC")
    fun findByContentContainingIgnoreCase(@Param("content") content: String, pageable: Pageable): Page<PostComment>
    
    /**
     * Find comments within a date range
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user LEFT JOIN FETCH pc.post WHERE pc.date BETWEEN :startDate AND :endDate ORDER BY pc.date DESC")
    fun findByDateBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<PostComment>
    
    /**
     * Find comments within a date range with pagination
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user LEFT JOIN FETCH pc.post WHERE pc.date BETWEEN :startDate AND :endDate ORDER BY pc.date DESC")
    fun findByDateBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime, pageable: Pageable): Page<PostComment>
    
    /**
     * Find comments for a specific post within a date range
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user WHERE pc.post = :post AND pc.date BETWEEN :startDate AND :endDate ORDER BY pc.date ASC")
    fun findByPostAndDateBetween(@Param("post") post: Post, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<PostComment>
    
    /**
     * Find comments by a specific user within a date range
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.post WHERE pc.user = :user AND pc.date BETWEEN :startDate AND :endDate ORDER BY pc.date DESC")
    fun findByUserAndDateBetween(@Param("user") user: User, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<PostComment>
    
    /**
     * Find recent comments (comments from the last N days)
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user LEFT JOIN FETCH pc.post WHERE pc.date >= :fromDate ORDER BY pc.date DESC")
    fun findRecentComments(@Param("fromDate") fromDate: LocalDateTime): List<PostComment>
    
    /**
     * Find recent comments with pagination
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user LEFT JOIN FETCH pc.post WHERE pc.date >= :fromDate ORDER BY pc.date DESC")
    fun findRecentComments(@Param("fromDate") fromDate: LocalDateTime, pageable: Pageable): Page<PostComment>
    
    /**
     * Find recent comments for a specific post
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user WHERE pc.post = :post AND pc.date >= :fromDate ORDER BY pc.date DESC")
    fun findRecentCommentsForPost(@Param("post") post: Post, @Param("fromDate") fromDate: LocalDateTime): List<PostComment>
    
    /**
     * Find the most recent comment for a post
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user WHERE pc.post = :post ORDER BY pc.date DESC LIMIT 1")
    fun findMostRecentCommentForPost(@Param("post") post: Post): PostComment?
    
    /**
     * Find the first comment for a post
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user WHERE pc.post = :post ORDER BY pc.date ASC LIMIT 1")
    fun findFirstCommentForPost(@Param("post") post: Post): PostComment?
    
    /**
     * Find comments ordered by date (newest first)
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user LEFT JOIN FETCH pc.post ORDER BY pc.date DESC")
    fun findAllOrderByDateDesc(): List<PostComment>
    
    /**
     * Find comments ordered by date with pagination
     */
    @Query("SELECT pc FROM PostComment pc LEFT JOIN FETCH pc.user LEFT JOIN FETCH pc.post ORDER BY pc.date DESC")
    fun findAllOrderByDateDesc(pageable: Pageable): Page<PostComment>
    
    /**
     * Count comments for a specific post
     */
    @Query("SELECT COUNT(pc) FROM PostComment pc WHERE pc.post = :post")
    fun countByPost(@Param("post") post: Post): Long
    
    /**
     * Count comments by a specific user
     */
    @Query("SELECT COUNT(pc) FROM PostComment pc WHERE pc.user = :user")
    fun countByUser(@Param("user") user: User): Long
    
    /**
     * Count comments within a date range
     */
    @Query("SELECT COUNT(pc) FROM PostComment pc WHERE pc.date BETWEEN :startDate AND :endDate")
    fun countByDateBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): Long
    
    /**
     * Count comments for a post within a date range
     */
    @Query("SELECT COUNT(pc) FROM PostComment pc WHERE pc.post = :post AND pc.date BETWEEN :startDate AND :endDate")
    fun countByPostAndDateBetween(@Param("post") post: Post, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): Long
    
    /**
     * Count comments by a user within a date range
     */
    @Query("SELECT COUNT(pc) FROM PostComment pc WHERE pc.user = :user AND pc.date BETWEEN :startDate AND :endDate")
    fun countByUserAndDateBetween(@Param("user") user: User, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): Long
    
    /**
     * Count recent comments (from the last N days)
     */
    @Query("SELECT COUNT(pc) FROM PostComment pc WHERE pc.date >= :fromDate")
    fun countRecentComments(@Param("fromDate") fromDate: LocalDateTime): Long
    
    /**
     * Find most active commenters (users with most comments)
     */
    @Query("SELECT pc.user, COUNT(pc) as commentCount FROM PostComment pc GROUP BY pc.user ORDER BY commentCount DESC")
    fun findMostActiveCommenters(pageable: Pageable): Page<Array<Any>>
    
    /**
     * Find posts with most comments
     */
    @Query("SELECT pc.post, COUNT(pc) as commentCount FROM PostComment pc GROUP BY pc.post ORDER BY commentCount DESC")
    fun findPostsWithMostComments(pageable: Pageable): Page<Array<Any>>
    
    /**
     * Find average comments per post
     */
    @Query("SELECT AVG(commentCount) FROM (SELECT COUNT(pc) as commentCount FROM PostComment pc GROUP BY pc.post) as subquery")
    fun findAverageCommentsPerPost(): Double?
    
    /**
     * Delete comments older than a specific date
     */
    @Query("DELETE FROM PostComment pc WHERE pc.date < :cutoffDate")
    fun deleteCommentsOlderThan(@Param("cutoffDate") cutoffDate: LocalDateTime): Int
}