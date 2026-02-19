package rfm.com.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import rfm.com.entity.YouTubeVideo

@Repository
interface YouTubeVideoRepository : MongoRepository<YouTubeVideo, String> {
    fun findByActiveOrderByDisplayOrderAsc(active: Boolean): List<YouTubeVideo>
    fun findByActiveTrueOrderByDisplayOrderAsc(): List<YouTubeVideo>
}
