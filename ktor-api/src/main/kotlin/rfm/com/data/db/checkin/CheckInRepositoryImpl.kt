
package rfm.com.data.db.checkin

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

class CheckInRepositoryImpl : CheckInRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun getCheckIn(checkInId: Int): CheckIn? = dbQuery {
        CheckInsTable
            .select { CheckInsTable.id eq checkInId }
            .map { it.toCheckIn() }
            .singleOrNull()
    }

    override suspend fun getCheckInsByKid(kidId: Int): List<CheckIn> = dbQuery {
        CheckInsTable
            .select { CheckInsTable.childId eq kidId }
            .map { it.toCheckIn() }
    }

    override suspend fun getCheckInsByService(serviceId: Int): List<CheckIn> = dbQuery {
        CheckInsTable
            .select { CheckInsTable.serviceId eq serviceId }
            .map { it.toCheckIn() }
    }

    override suspend fun addCheckIn(checkIn: CheckIn): Boolean = dbQuery {
        CheckInsTable.insert {
            it[childId] = checkIn.childId
            it[checkInTime] = LocalDateTime.parse(checkIn.checkInTime)
            it[serviceId] = checkIn.serviceId
            it[guardianCode] = checkIn.guardianCode
            it[status] = checkIn.status
        }.insertedCount > 0
    }

    override suspend fun updateCheckIn(checkIn: CheckIn): Boolean = dbQuery {
        CheckInsTable.update({ CheckInsTable.id eq checkIn.id }) {
            it[childId] = checkIn.childId
            it[checkInTime] = LocalDateTime.parse(checkIn.checkInTime)
            it[checkOutTime] = if (checkIn.checkOutTime != null) LocalDateTime.parse(checkIn.checkOutTime) else null
            it[serviceId] = checkIn.serviceId
            it[guardianCode] = checkIn.guardianCode
            it[status] = checkIn.status
        } > 0
    }
}
