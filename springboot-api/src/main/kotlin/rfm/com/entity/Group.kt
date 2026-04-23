package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Embedded location for a connection group.
 *
 * `coordinates` is a GeoJSON point: [longitude, latitude]. The field is
 * annotated with a 2dsphere index so MongoDB can answer $nearSphere
 * queries for "groups near me".
 */
data class GroupLocation(
    val addressLine: String,
    @Indexed
    val city: String,
    val region: String? = null,
    val postalCode: String? = null,
    val country: String = "PT",
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    val coordinates: DoubleArray
) {
    val longitude: Double get() = coordinates[0]
    val latitude: Double get() = coordinates[1]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GroupLocation
        return addressLine == other.addressLine &&
            city == other.city &&
            region == other.region &&
            postalCode == other.postalCode &&
            country == other.country &&
            coordinates.contentEquals(other.coordinates)
    }

    override fun hashCode(): Int {
        var result = addressLine.hashCode()
        result = 31 * result + city.hashCode()
        result = 31 * result + (region?.hashCode() ?: 0)
        result = 31 * result + (postalCode?.hashCode() ?: 0)
        result = 31 * result + country.hashCode()
        result = 31 * result + coordinates.contentHashCode()
        return result
    }
}

enum class MeetingFrequency {
    WEEKLY,
    BIWEEKLY,
    MONTHLY
}

@Document(collection = "groups")
data class Group(
    @Id
    val id: String? = null,

    val name: String,

    @Indexed
    val ministry: Ministry,

    val description: String,

    val leaderName: String,

    val leaderContact: String,

    val meetingDay: DayOfWeek,

    val meetingTime: LocalTime,

    val frequency: MeetingFrequency = MeetingFrequency.WEEKLY,

    val location: GroupLocation,

    val imagePath: String? = null,

    val maxMembers: Int? = null,

    val currentMembers: Int = 0,

    @Indexed
    val isActive: Boolean = true,

    val isJoinable: Boolean = true,

    val tags: List<String> = emptyList(),

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Group
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "Group(id=$id, name='$name', ministry=$ministry, city='${location.city}')"
}
