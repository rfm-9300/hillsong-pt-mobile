
package rfm.com.data.db.kid

interface KidRepository {
    suspend fun getKid(kidId: Int): Kid?
    suspend fun getKidsByFamily(familyId: Int): List<Kid>
    suspend fun addKid(kid: Kid): Boolean
    suspend fun updateKid(kid: Kid): Boolean
    suspend fun deleteKid(kidId: Int): Boolean
}
