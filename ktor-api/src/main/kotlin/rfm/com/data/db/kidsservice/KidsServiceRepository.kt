package rfm.com.data.db.kidsservice

interface KidsServiceRepository {
    // Kids Service management
    suspend fun getAllKidsServices(): List<KidsService>
    suspend fun getKidsServiceById(id: Int): KidsService?
    suspend fun getKidsServicesByServiceId(serviceId: Int): List<KidsService>
    suspend fun getActiveKidsServices(): List<KidsService>
    suspend fun createKidsService(kidsService: KidsService): KidsService?
    suspend fun updateKidsService(kidsService: KidsService): Boolean
    suspend fun deleteKidsService(id: Int): Boolean
    suspend fun activateKidsService(id: Int): Boolean
    suspend fun deactivateKidsService(id: Int): Boolean
    
    // Kids Check-in management
    suspend fun checkInKid(kidsCheckIn: KidsCheckIn): KidsCheckIn?
    suspend fun checkOutKid(checkInId: Int, checkedOutBy: Int, notes: String = ""): Boolean
    suspend fun getActiveCheckIns(kidsServiceId: Int): List<KidsCheckIn>
    suspend fun getCheckInHistory(kidId: Int): List<KidsCheckIn>
    suspend fun getCheckInById(id: Int): KidsCheckIn?
    suspend fun updateCheckInStatus(checkInId: Int, status: CheckInStatus): Boolean
    suspend fun addCheckInNotes(checkInId: Int, notes: String): Boolean
    suspend fun getKidsServiceCapacity(kidsServiceId: Int): Pair<Int, Int> // current count, max capacity
}