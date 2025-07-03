package example.com.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventResponse (
    val success : Boolean,
    val message : String,
){
    companion object {
        const val SUCCESS = "Event created successfully"
        const val FAILURE = "Failed to create event"

        fun success() = CreateEventResponse(true, SUCCESS)
        fun failure() = CreateEventResponse(false, FAILURE)
    }
}