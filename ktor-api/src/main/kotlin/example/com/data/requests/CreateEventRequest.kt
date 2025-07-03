package example.com.data.requests
import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest (
    val source : String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val image: ByteArray? = null
)