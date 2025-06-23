package rfm.hillsongptapp.core.data.repository.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Delete

@Entity(tableName = "userprofile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val joinedAt: Long, // Store as epoch millis
    val imagePath: String,
    val isAdmin: Boolean = false
)

@Dao
interface UserProfileDao {
    @Upsert
    suspend fun insertUserProfile(profile: UserProfile)

    @Query("SELECT * FROM userprofile WHERE userId = :userId")
    suspend fun getUserProfileByUserId(userId: Long): UserProfile?

    @Delete
    suspend fun deleteUserProfile(profile: UserProfile)
} 