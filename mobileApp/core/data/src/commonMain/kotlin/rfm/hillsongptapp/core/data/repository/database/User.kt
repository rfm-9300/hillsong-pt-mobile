package rfm.hillsongptapp.core.data.repository.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val token: String? = null,
    val expiryAt: Long? = null, // Store as epoch millis
    val refreshToken: String? = null, // For refresh token if your API supports it
    val lastLoginAt: Long? = null // Track last login time
)

@Dao
interface UserDao {

    @Upsert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT * FROM user ORDER BY lastLoginAt DESC LIMIT 1")
    suspend fun getCurrentUser(): User?

    @Query("SELECT * FROM user WHERE token IS NOT NULL ORDER BY lastLoginAt DESC LIMIT 1")
    suspend fun getAuthenticatedUser(): User?

    @Query("DELETE FROM user")
    suspend fun clearAllUsers()

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}
