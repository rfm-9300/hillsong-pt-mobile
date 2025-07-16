
package rfm.com.data.db.kid

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate

class KidRepositoryImpl : KidRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun getKid(kidId: Int): Kid? = dbQuery {
        KidsTable
            .select { KidsTable.id eq kidId }
            .map { it.toKid() }
            .singleOrNull()
    }

    override suspend fun getKidsByFamily(familyId: Int): List<Kid> = dbQuery {
        KidsTable
            .select { KidsTable.familyId eq familyId }
            .map { it.toKid() }
    }

    override suspend fun addKid(kid: Kid): Boolean = dbQuery {
        KidsTable.insert {
            it[familyId] = kid.familyId
            it[firstName] = kid.firstName
            it[lastName] = kid.lastName
            it[dateOfBirth] = LocalDate.parse(kid.dateOfBirth)
            it[allergies] = kid.allergies
            it[notes] = kid.notes
        }.insertedCount > 0
    }

    override suspend fun updateKid(kid: Kid): Boolean = dbQuery {
        KidsTable.update({ KidsTable.id eq kid.id }) {
            it[familyId] = kid.familyId
            it[firstName] = kid.firstName
            it[lastName] = kid.lastName
            it[dateOfBirth] = LocalDate.parse(kid.dateOfBirth)
            it[allergies] = kid.allergies
            it[notes] = kid.notes
        } > 0
    }

    override suspend fun deleteKid(kidId: Int): Boolean = dbQuery {
        KidsTable.deleteWhere { KidsTable.id eq kidId } > 0
    }
}
