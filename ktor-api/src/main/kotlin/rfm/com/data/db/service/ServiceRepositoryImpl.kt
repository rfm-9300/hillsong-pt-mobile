package rfm.com.data.db.service

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

class ServiceRepositoryImpl : ServiceRepository {
    
    private suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, statement = block)

    override suspend fun getAllServices(): List<Service> = suspendTransaction {
        ServiceTable.selectAll()
            .orderBy(ServiceTable.startTime)
            .map { it.toService() }
    }

    override suspend fun getServiceById(id: Int): Service? = suspendTransaction {
        ServiceTable.select { ServiceTable.id eq id }
            .singleOrNull()?.toService()
    }

    override suspend fun getActiveServices(): List<Service> = suspendTransaction {
        ServiceTable.select { ServiceTable.isActive eq true }
            .orderBy(ServiceTable.startTime)
            .map { it.toService() }
    }

    override suspend fun getServicesByType(serviceType: ServiceType): List<Service> = suspendTransaction {
        ServiceTable.select { 
            (ServiceTable.serviceType eq serviceType.name) and 
            (ServiceTable.isActive eq true) 
        }
        .orderBy(ServiceTable.startTime)
        .map { it.toService() }
    }

    override suspend fun createService(service: Service): Service? = suspendTransaction {
        try {
            val serviceId = ServiceTable.insert {
                it[name] = service.name
                it[description] = service.description
                it[startTime] = service.startTime
                it[endTime] = service.endTime
                it[location] = service.location
                it[serviceType] = service.serviceType.name
                it[isActive] = service.isActive
                it[createdAt] = service.createdAt
            } get ServiceTable.id

            service.copy(id = serviceId.value)
        } catch (e: Exception) {
            println("Error creating service: ${e.message}")
            null
        }
    }

    override suspend fun updateService(service: Service): Boolean = suspendTransaction {
        try {
            ServiceTable.update({ ServiceTable.id eq service.id }) {
                it[name] = service.name
                it[description] = service.description
                it[startTime] = service.startTime
                it[endTime] = service.endTime
                it[location] = service.location
                it[serviceType] = service.serviceType.name
                it[isActive] = service.isActive
            } > 0
        } catch (e: Exception) {
            println("Error updating service: ${e.message}")
            false
        }
    }

    override suspend fun deleteService(id: Int): Boolean = suspendTransaction {
        try {
            ServiceTable.deleteWhere { ServiceTable.id eq id } > 0
        } catch (e: Exception) {
            println("Error deleting service: ${e.message}")
            false
        }
    }

    override suspend fun activateService(id: Int): Boolean = suspendTransaction {
        try {
            ServiceTable.update({ ServiceTable.id eq id }) {
                it[isActive] = true
            } > 0
        } catch (e: Exception) {
            println("Error activating service: ${e.message}")
            false
        }
    }

    override suspend fun deactivateService(id: Int): Boolean = suspendTransaction {
        try {
            ServiceTable.update({ ServiceTable.id eq id }) {
                it[isActive] = false
            } > 0
        } catch (e: Exception) {
            println("Error deactivating service: ${e.message}")
            false
        }
    }
}