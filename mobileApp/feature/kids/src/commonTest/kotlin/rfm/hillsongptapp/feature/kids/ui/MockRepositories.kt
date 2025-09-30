package rfm.hillsongptapp.feature.kids.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import rfm.hillsongptapp.core.data.model.AttendanceReport
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.CheckInRecord
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.model.ServiceReport
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.core.data.repository.KidsRepository
import rfm.hillsongptapp.core.data.repository.KidsResult
import rfm.hillsongptapp.core.data.repository.database.User

/**
 * Mock implementation of KidsRepository for ViewModel testing
 */
class MockKidsRepository : KidsRepository {
    
    // Configurable results for different operations
    var getChildrenForParentResult: KidsResult<List<Child>> = KidsResult.Error("Not configured")
    var registerChildResult: KidsResult<Child> = KidsResult.Error("Not configured")
    var updateChildResult: KidsResult<Child> = KidsResult.Error("Not configured")
    var deleteChildResult: KidsResult<Unit> = KidsResult.Error("Not configured")
    var getChildByIdResult: KidsResult<Child> = KidsResult.Error("Not configured")
    
    var getAvailableServicesResult: KidsResult<List<KidsService>> = KidsResult.Error("Not configured")
    var getServicesForAgeResult: KidsResult<List<KidsService>> = KidsResult.Error("Not configured")
    var getServiceByIdResult: KidsResult<KidsService> = KidsResult.Error("Not configured")
    var getServicesAcceptingCheckInsResult: KidsResult<List<KidsService>> = KidsResult.Error("Not configured")
    
    var checkInChildResult: KidsResult<CheckInRecord> = KidsResult.Error("Not configured")
    var checkOutChildResult: KidsResult<CheckInRecord> = KidsResult.Error("Not configured")
    var getCheckInHistoryResult: KidsResult<List<CheckInRecord>> = KidsResult.Error("Not configured")
    var getCurrentCheckInsResult: KidsResult<List<CheckInRecord>> = KidsResult.Error("Not configured")
    var getAllCurrentCheckInsResult: KidsResult<List<CheckInRecord>> = KidsResult.Error("Not configured")
    var getCheckInRecordResult: KidsResult<CheckInRecord> = KidsResult.Error("Not configured")
    
    var getServiceReportResult: KidsResult<ServiceReport> = KidsResult.Error("Not configured")
    var getAttendanceReportResult: KidsResult<AttendanceReport> = KidsResult.Error("Not configured")
    
    // Track method calls for verification
    val getChildrenForParentCalls = mutableListOf<String>()
    val registerChildCalls = mutableListOf<Child>()
    val updateChildCalls = mutableListOf<Child>()
    val deleteChildCalls = mutableListOf<String>()
    val getChildByIdCalls = mutableListOf<String>()
    
    val getServicesForAgeCalls = mutableListOf<Int>()
    val getServiceByIdCalls = mutableListOf<String>()
    
    val checkInChildCalls = mutableListOf<Tuple4<String, String, String, String?>>()
    val checkOutChildCalls = mutableListOf<Pair<String, String>>()
    val getCheckInHistoryCalls = mutableListOf<Pair<String, Int?>>()
    val getCurrentCheckInsCalls = mutableListOf<String>()
    val getCheckInRecordCalls = mutableListOf<String>()
    
    val getServiceReportCalls = mutableListOf<String>()
    val getAttendanceReportCalls = mutableListOf<Pair<String, String>>()
    
    // Child Management Operations
    override suspend fun getChildrenForParent(parentId: String): KidsResult<List<Child>> {
        getChildrenForParentCalls.add(parentId)
        return getChildrenForParentResult
    }
    
    override suspend fun registerChild(child: Child): KidsResult<Child> {
        registerChildCalls.add(child)
        return registerChildResult
    }
    
    override suspend fun updateChild(child: Child): KidsResult<Child> {
        updateChildCalls.add(child)
        return updateChildResult
    }
    
    override suspend fun deleteChild(childId: String): KidsResult<Unit> {
        deleteChildCalls.add(childId)
        return deleteChildResult
    }
    
    override suspend fun getChildById(childId: String): KidsResult<Child> {
        getChildByIdCalls.add(childId)
        return getChildByIdResult
    }
    
    // Service Management Operations
    override suspend fun getAvailableServices(): KidsResult<List<KidsService>> {
        return getAvailableServicesResult
    }
    
    override suspend fun getServicesForAge(age: Int): KidsResult<List<KidsService>> {
        getServicesForAgeCalls.add(age)
        return getServicesForAgeResult
    }
    
    override suspend fun getServiceById(serviceId: String): KidsResult<KidsService> {
        getServiceByIdCalls.add(serviceId)
        return getServiceByIdResult
    }
    
    override suspend fun getServicesAcceptingCheckIns(): KidsResult<List<KidsService>> {
        return getServicesAcceptingCheckInsResult
    }
    
    // Check-in/Check-out Operations
    override suspend fun checkInChild(
        childId: String,
        serviceId: String,
        checkedInBy: String,
        notes: String?
    ): KidsResult<CheckInRecord> {
        checkInChildCalls.add(Tuple4(childId, serviceId, checkedInBy, notes))
        return checkInChildResult
    }
    
    override suspend fun checkOutChild(
        childId: String,
        checkedOutBy: String,
        notes: String?
    ): KidsResult<CheckInRecord> {
        checkOutChildCalls.add(childId to checkedOutBy)
        return checkOutChildResult
    }
    
    override suspend fun getCheckInHistory(childId: String, limit: Int?): KidsResult<List<CheckInRecord>> {
        getCheckInHistoryCalls.add(childId to limit)
        return getCheckInHistoryResult
    }
    
    override suspend fun getCurrentCheckIns(serviceId: String): KidsResult<List<CheckInRecord>> {
        getCurrentCheckInsCalls.add(serviceId)
        return getCurrentCheckInsResult
    }
    
    override suspend fun getAllCurrentCheckIns(): KidsResult<List<CheckInRecord>> {
        return getAllCurrentCheckInsResult
    }
    
    override suspend fun getCheckInRecord(recordId: String): KidsResult<CheckInRecord> {
        getCheckInRecordCalls.add(recordId)
        return getCheckInRecordResult
    }
    
    // Staff/Reporting Operations
    override suspend fun getServiceReport(serviceId: String): KidsResult<ServiceReport> {
        getServiceReportCalls.add(serviceId)
        return getServiceReportResult
    }
    
    override suspend fun getAttendanceReport(startDate: String, endDate: String): KidsResult<AttendanceReport> {
        getAttendanceReportCalls.add(startDate to endDate)
        return getAttendanceReportResult
    }
    
    // Real-time Operations (Flow-based for reactive updates)
    override fun getChildrenForParentStream(parentId: String): Flow<KidsResult<List<Child>>> {
        return flowOf(getChildrenForParentResult)
    }
    
    override fun getAvailableServicesStream(): Flow<KidsResult<List<KidsService>>> {
        return flowOf(getAvailableServicesResult)
    }
    
    override fun getCurrentCheckInsStream(serviceId: String): Flow<KidsResult<List<CheckInRecord>>> {
        return flowOf(getCurrentCheckInsResult)
    }
    
    override fun getAllCurrentCheckInsStream(): Flow<KidsResult<List<CheckInRecord>>> {
        return flowOf(getAllCurrentCheckInsResult)
    }
    
    override fun subscribeToChildUpdates(childId: String): Flow<KidsResult<Child>> {
        return flowOf(getChildByIdResult)
    }
    
    override fun subscribeToServiceUpdates(serviceId: String): Flow<KidsResult<KidsService>> {
        return flowOf(getServiceByIdResult)
    }
}

/**
 * Mock implementation of AuthRepository for ViewModel testing
 */
class MockAuthRepository : AuthRepository {
    
    var getUserByIdResult: User? = null
    
    override suspend fun getUserById(id: Int): User? {
        return getUserByIdResult
    }
    
    // Stub implementations for other methods (not used in kids ViewModels)
    override suspend fun login(email: String, password: String): rfm.hillsongptapp.core.data.repository.AuthResult<rfm.hillsongptapp.core.data.model.User> {
        TODO("Not implemented for testing")
    }
    
    override suspend fun logout(): rfm.hillsongptapp.core.data.repository.AuthResult<Unit> {
        TODO("Not implemented for testing")
    }
    
    override suspend fun getCurrentUser(): rfm.hillsongptapp.core.data.repository.AuthResult<rfm.hillsongptapp.core.data.model.User> {
        TODO("Not implemented for testing")
    }
    
    override suspend fun refreshToken(): rfm.hillsongptapp.core.data.repository.AuthResult<String> {
        TODO("Not implemented for testing")
    }
    
    override suspend fun updateProfile(user: rfm.hillsongptapp.core.data.model.User): rfm.hillsongptapp.core.data.repository.AuthResult<rfm.hillsongptapp.core.data.model.User> {
        TODO("Not implemented for testing")
    }
    
    override suspend fun changePassword(currentPassword: String, newPassword: String): rfm.hillsongptapp.core.data.repository.AuthResult<Unit> {
        TODO("Not implemented for testing")
    }
    
    override suspend fun resetPassword(email: String): rfm.hillsongptapp.core.data.repository.AuthResult<Unit> {
        TODO("Not implemented for testing")
    }
    
    override suspend fun deleteAccount(): rfm.hillsongptapp.core.data.repository.AuthResult<Unit> {
        TODO("Not implemented for testing")
    }
    
    override suspend fun isLoggedIn(): Boolean {
        TODO("Not implemented for testing")
    }
}

/**
 * Helper data class for 4-tuple parameters
 */
data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)