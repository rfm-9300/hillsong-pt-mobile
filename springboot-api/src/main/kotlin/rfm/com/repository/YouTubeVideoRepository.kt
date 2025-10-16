package rfm.com.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import rfm.com.entity.YouTubeVideo

@Repository
interface YouTubeVideoRepository : JpaRepository<YouTubeVideo, Long> {
    fun findByActiveOrderByDisplayOrderAsc(active: Boolean): List<YouTubeVideo>
    fun findByActiveTrueOrderByDisplayOrderAsc(): List<YouTubeVideo>
}
