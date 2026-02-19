package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.Post
import java.time.LocalDateTime

@Repository
interface PostRepository : MongoRepository<Post, String> {
    
    fun findAllByOrderByDateDesc(pageable: Pageable): Page<Post>
    
    fun findByAuthorId(authorId: String): List<Post>
    
    fun findByAuthorId(authorId: String, pageable: Pageable): Page<Post>
    
    @Query("{'likedByUserIds': ?0}", sort = "{'date': -1}")
    fun findPostsLikedByUser(userId: String): List<Post>
    
    @Query("{'likedByUserIds': ?0}", sort = "{'date': -1}")
    fun findPostsLikedByUser(userId: String, pageable: Pageable): Page<Post>
    
    @Query("{'title': {'\$regex': ?0, '\$options': 'i'}}", sort = "{'date': -1}")
    fun findByTitleContainingIgnoreCase(title: String): List<Post>

    @Query("{'content': {'\$regex': ?0, '\$options': 'i'}}", sort = "{'date': -1}")
    fun findByContentContainingIgnoreCase(content: String): List<Post>
    
    @Query("{'date': {'\$gte': ?0, '\$lte': ?1}}", sort = "{'date': -1}")
    fun findPostsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Post>
    
    @Query("{'date': {'\$gte': ?0}}", sort = "{'date': -1}")
    fun findRecentPosts(fromDate: LocalDateTime): List<Post>
    
    @Query("{'date': {'\$gte': ?0}}", sort = "{'date': -1}")
    fun findRecentPosts(fromDate: LocalDateTime, pageable: Pageable): Page<Post>
    
    fun countByAuthorId(authorId: String): Long
    
    @Query(value = "{'date': {'\$gte': ?0}}", count = true)
    fun countPostsCreatedAfter(fromDate: LocalDateTime): Long
}