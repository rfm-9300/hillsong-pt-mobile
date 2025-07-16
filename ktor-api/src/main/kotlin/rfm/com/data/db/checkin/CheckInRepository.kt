
package rfm.com.data.db.checkin

interface CheckInRepository {
    suspend fun getCheckIn(checkInId: Int): CheckIn?
    suspend fun getCheckInsByKid(kidId: Int): List<CheckIn>
    suspend fun getCheckInsByService(serviceId: Int): List<CheckIn>
    suspend fun addCheckIn(checkIn: CheckIn): Boolean
    suspend fun updateCheckIn(checkIn: CheckIn): Boolean
}
