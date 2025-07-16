package rfm.com.data.db.kidsservice

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

class KidsServiceRepositoryImpl : KidsServiceRepository {
    
    private suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, statement = block)

    // Kids Service management
    override suspend fun getAllKidsServices(): List<KidsService> = suspendTransaction {
        KidsServiceTable.selectAll()
            .orderBy(KidsServiceTable.name)
            .map { it.toKidsService() }
    }

    override suspend fun getKidsServiceById(id: Int): KidsService? = suspendTransaction {
        KidsServiceTable.select { KidsServiceTable.id eq id }
            .singleOrNull()?.toKidsService()
    }

    override suspend fun getKidsServicesByServiceId(serviceId: Int): List<KidsService> = suspendTransaction {
        KidsServiceTable.select { 
            (KidsServiceTable.serviceId eq serviceId) and 
            (KidsServiceTable.isActive eq true) 
        }
        .orderBy(KidsServiceTable.ageGroupMin)
        .map { it.toKidsService() }
    }

    override suspend fun getActiveKidsServices(): List<KidsService> = suspendTransaction {
        KidsServiceTable.select { KidsServiceTable.isActive eq true }
            .orderBy(KidsServiceTable.name)
            .map { it.toKidsService() }
    }

    override suspend fun createKidsService(kidsService: KidsService): KidsService? = suspendTransaction {
        try {
            val kidsServiceId = KidsServiceTable.insert {
                it[serviceId] = kidsService.serviceId
                it[name] = kidsService.name
                it[description] = kidsService.description
                it[ageGroupMin] = kidsService.ageGroupMin
                it[ageGroupMax] = kidsService.ageGroupMax
                it[maxCapacity] = kidsService.maxCapacity
                it[location] = kidsService.location
                it[isActive] = kidsService.isActive
                it[createdAt] = kidsService.createdAt
            } get KidsServiceTable.id

            kidsService.copy(id = kidsServiceId.value)
        } catch (e: Exception) {
            println("Error creating kids service: ${e.message}")
            null
        }
    }

    override suspend fun updateKidsService(kidsService: KidsService): Boolean = suspendTransaction {
        try {
            KidsServiceTable.update({ KidsServiceTable.id eq kidsService.id }) {
                it[serviceId] = kidsService.serviceId
                it[name] = kidsService.name
                it[description] = kidsService.description
                it[ageGroupMin] = kidsService.ageGroupMin
                it[ageGroupMax] = kidsService.ageGroupMax
                it[maxCapacity] = kidsService.maxCapacity
                it[location] = kidsService.location
                it[isActive] = kidsService.isActive
            } > 0
        } catch (e: Exception) {
            println("Error updating kids service: ${e.message}")
            false
        }
    }

    override suspend fun deleteKidsService(id: Int): Boolean = suspendTransaction {
        try {
            KidsServiceTable.deleteWhere { KidsServiceTable.id eq id } > 0
        } catch (e: Exception) {
            println("Error deleting kids service: ${e.message}")
            false
        }
    }

    override suspend fun activateKidsService(id: Int): Boolean = suspendTransaction {
        try {
            KidsServiceTable.update({ KidsServiceTable.id eq id }) {
                it[isActive] = true
            } > 0
        } catch (e: Exception) {
            println("Error activating kids service: ${e.message}")
            false
        }
    }

    override suspend fun deactivateKidsService(id: Int): Boolean = suspendTransaction {
        try {
            KidsServiceTable.update({ KidsServiceTable.id eq id }) {
                it[isActive] = false
            } > 0
        } catch (e: Exception) {
            println("Error deactivating kids service: ${e.message}")
            false
        }
    }

    // Kids Check-in management
    override suspend fun checkInKid(kidsCheckIn: KidsCheckIn): KidsCheckIn? = suspendTransaction {
        try {
            // Check if kid is already checked in to this service
            val existingCheckIn = KidsCheckInTable.select {
                (KidsCheckInTable.kidsServiceId eq kidsCheckIn.kidsServiceId) and
                (KidsCheckInTable.kidId eq kidsCheckIn.kidId) and
                (KidsCheckInTable.status eq CheckInStatus.CHECKED_IN.name)
            }.singleOrNull()

            if (existingCheckIn != null) {
                println("Kid is already checked in to this service")
                return@suspendTransaction null
            }

            val checkInId = KidsCheckInTable.insert {
                it[kidsServiceId] = kidsCheckIn.kidsServiceId
                it[kidId] = kidsCheckIn.kidId
                it[checkedInBy] = kidsCheckIn.checkedInBy
                it[checkInTime] = kidsCheckIn.checkInTime
                it[notes] = kidsCheckIn.notes
                it[status] = kidsCheckIn.status.name
            } get KidsCheckInTable.id

            kidsCheckIn.copy(id = checkInId.value)
        } catch (e: Exception) {
            println("Error checking in kid: ${e.message}")
            null
        }
    }

    override suspend fun checkOutKid(checkInId: Int, checkedOutBy: Int, notes: String): Boolean = suspendTransaction {
        try {
            KidsCheckInTable.update({ KidsCheckInTable.id eq checkInId }) {
                it[checkOutTime] = LocalDateTime.now()
                it[KidsCheckInTable.checkedOutBy] = checkedOutBy
                it[status] = CheckInStatus.CHECKED_OUT.name
                if (notes.isNotEmpty()) {
                    it[KidsCheckInTable.notes] = notes
                }
            } > 0
        } catch (e: Exception) {
            println("Error checking out kid: ${e.message}")
            false
        }
    }

    override suspend fun getActiveCheckIns(kidsServiceId: Int): List<KidsCheckIn> = suspendTransaction {
        KidsCheckInTable.select {
            (KidsCheckInTable.kidsServiceId eq kidsServiceId) and
            (KidsCheckInTable.status eq CheckInStatus.CHECKED_IN.name)
        }
        .orderBy(KidsCheckInTable.checkInTime)
        .map { it.toKidsCheckIn() }
    }

    override suspend fun getCheckInHistory(kidId: Int): List<KidsCheckIn> = suspendTransaction {
        KidsCheckInTable.select { KidsCheckInTable.kidId eq kidId }
            .orderBy(KidsCheckInTable.checkInTime, SortOrder.DESC)
            .map { it.toKidsCheckIn() }
    }

    override suspend fun getCheckInById(id: Int): KidsCheckIn? = suspendTransaction {
        KidsCheckInTable.select { KidsCheckInTable.id eq id }
            .singleOrNull()?.toKidsCheckIn()
    }

    override suspend fun updateCheckInStatus(checkInId: Int, status: CheckInStatus): Boolean = suspendTransaction {
        try {
            KidsCheckInTable.update({ KidsCheckInTable.id eq checkInId }) {
                it[KidsCheckInTable.status] = status.name
            } > 0
        } catch (e: Exception) {
            println("Error updating check-in status: ${e.message}")
            false
        }
    }

    override suspend fun addCheckInNotes(checkInId: Int, notes: String): Boolean = suspendTransaction {
        try {
            KidsCheckInTable.update({ KidsCheckInTable.id eq checkInId }) {
                it[KidsCheckInTable.notes] = notes
            } > 0
        } catch (e: Exception) {
            println("Error adding check-in notes: ${e.message}")
            false
        }
    }

    override suspend fun getKidsServiceCapacity(kidsServiceId: Int): Pair<Int, Int> = suspendTransaction {
        try {
            val kidsService = KidsServiceTable.select { KidsServiceTable.id eq kidsServiceId }
                .singleOrNull()?.toKidsService()
                
            if (kidsService == null) {
                return@suspendTransaction Pair(0, 0)
            }

            val currentCount = KidsCheckInTable.select {
                (KidsCheckInTable.kidsServiceId eq kidsServiceId) and
                (KidsCheckInTable.status eq CheckInStatus.CHECKED_IN.name)
            }.count().toInt()

            Pair(currentCount, kidsService.maxCapacity)
        } catch (e: Exception) {
            println("Error getting kids service capacity: ${e.message}")
            Pair(0, 0)
        }
    }
}