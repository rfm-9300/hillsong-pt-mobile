package rfm.com.data.db.service

interface ServiceRepository {
    suspend fun getAllServices(): List<Service>
    suspend fun getServiceById(id: Int): Service?
    suspend fun getActiveServices(): List<Service>
    suspend fun getServicesByType(serviceType: ServiceType): List<Service>
    suspend fun createService(service: Service): Service?
    suspend fun updateService(service: Service): Boolean
    suspend fun deleteService(id: Int): Boolean
    suspend fun activateService(id: Int): Boolean
    suspend fun deactivateService(id: Int): Boolean
}