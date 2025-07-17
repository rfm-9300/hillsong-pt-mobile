package rfm.com.data.db.attendance

import rfm.com.data.db.event.EventTable
import rfm.com.data.db.service.ServiceTable
import rfm.com.data.db.kidsservice.KidsServiceTable
import rfm.com.data.db.user.UserTable
import rfm.com.data.db.user.UserProfilesTable
import rfm.com.data.db.kid.KidTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import rfm.com.plugins.Logger

class AttendanceRepositoryImpl : AttendanceRepository {
    
    override suspend fun checkInUser(
        eventType: EventType,
        eventId: Int,
        userId: Int,
        checkedInBy: Int,
        notes: String
    ): Attendance? = transaction {
        // Check if user is already checked in
        val existingAttendance = AttendanceTable.select {
            (AttendanceTable.eventType eq eventType.name) and
            (AttendanceTable.eventId eq eventId) and
            (AttendanceTable.userId eq userId) and
            (AttendanceTable.status eq AttendanceStatus.CHECKED_IN.name)
        }.firstOrNull()
        
        if (existingAttendance != null) {
            return@transaction existingAttendance.toAttendance()
        }
        
        // Create new attendance record
        val id = AttendanceTable.insert {
            it[AttendanceTable.eventType] = eventType.name
            it[AttendanceTable.eventId] = eventId
            it[AttendanceTable.userId] = userId
            it[AttendanceTable.kidId] = null
            it[AttendanceTable.checkedInBy] = checkedInBy
            it[AttendanceTable.checkInTime] = LocalDateTime.now()
            it[AttendanceTable.notes] = notes
            it[AttendanceTable.status] = AttendanceStatus.CHECKED_IN.name
        } get AttendanceTable.id
        
        AttendanceTable.select { AttendanceTable.id eq id }.firstOrNull()?.toAttendance()
    }
    
    override suspend fun checkInKid(
        eventType: EventType,
        eventId: Int,
        kidId: Int,
        checkedInBy: Int,
        notes: String
    ): Attendance? = transaction {
        // Check if kid is already checked in
        val existingAttendance = AttendanceTable.select {
            (AttendanceTable.eventType eq eventType.name) and
            (AttendanceTable.eventId eq eventId) and
            (AttendanceTable.kidId eq kidId) and
            (AttendanceTable.status eq AttendanceStatus.CHECKED_IN.name)
        }.firstOrNull()
        
        if (existingAttendance != null) {
            return@transaction existingAttendance.toAttendance()
        }
        
        // Create new attendance record
        val id = AttendanceTable.insert {
            it[AttendanceTable.eventType] = eventType.name
            it[AttendanceTable.eventId] = eventId
            it[AttendanceTable.userId] = null
            it[AttendanceTable.kidId] = kidId
            it[AttendanceTable.checkedInBy] = checkedInBy
            it[AttendanceTable.checkInTime] = LocalDateTime.now()
            it[AttendanceTable.notes] = notes
            it[AttendanceTable.status] = AttendanceStatus.CHECKED_IN.name
        } get AttendanceTable.id
        
        AttendanceTable.select { AttendanceTable.id eq id }.firstOrNull()?.toAttendance()
    }
    
    override suspend fun checkOutUser(
        attendanceId: Int,
        checkedOutBy: Int,
        notes: String
    ): Boolean = transaction {
        val attendance = AttendanceTable.select { AttendanceTable.id eq attendanceId }.firstOrNull()
            ?: return@transaction false
        
        if (attendance[AttendanceTable.status] != AttendanceStatus.CHECKED_IN.name) {
            return@transaction false
        }
        
        val updatedRows = AttendanceTable.update({ AttendanceTable.id eq attendanceId }) {
            it[AttendanceTable.status] = AttendanceStatus.CHECKED_OUT.name
            it[AttendanceTable.checkOutTime] = LocalDateTime.now()
            it[AttendanceTable.checkedOutBy] = checkedOutBy
            if (notes.isNotEmpty()) {
                it[AttendanceTable.notes] = attendance[AttendanceTable.notes] + "\nCheck-out: $notes"
            }
        }
        
        updatedRows > 0
    }
    
    override suspend fun checkOutKid(
        attendanceId: Int,
        checkedOutBy: Int,
        notes: String
    ): Boolean = transaction {
        val attendance = AttendanceTable.select { AttendanceTable.id eq attendanceId }.firstOrNull()
            ?: return@transaction false
        
        if (attendance[AttendanceTable.status] != AttendanceStatus.CHECKED_IN.name) {
            return@transaction false
        }
        
        val updatedRows = AttendanceTable.update({ AttendanceTable.id eq attendanceId }) {
            it[AttendanceTable.status] = AttendanceStatus.CHECKED_OUT.name
            it[AttendanceTable.checkOutTime] = LocalDateTime.now()
            it[AttendanceTable.checkedOutBy] = checkedOutBy
            if (notes.isNotEmpty()) {
                it[AttendanceTable.notes] = attendance[AttendanceTable.notes] + "\nCheck-out: $notes"
            }
        }
        
        updatedRows > 0
    }
    
    override suspend fun getAttendanceByEvent(
        eventType: EventType,
        eventId: Int
    ): List<AttendanceWithDetails> = transaction {
        val checkedInByProfileAlias = UserProfilesTable.alias("checkedInByProfile")
        val checkedOutByProfileAlias = UserProfilesTable.alias("checkedOutByProfile")
        
        val attendances = AttendanceTable
            .leftJoin(UserTable, { AttendanceTable.userId }, { UserTable.id })
            .leftJoin(UserProfilesTable, { AttendanceTable.userId }, { UserProfilesTable.userId })
            .leftJoin(KidTable, { AttendanceTable.kidId }, { KidTable.id })
            .leftJoin(UserTable.alias("checkedInBy"), { AttendanceTable.checkedInBy }, { UserTable.alias("checkedInBy")[UserTable.id] })
            .leftJoin(checkedInByProfileAlias, { AttendanceTable.checkedInBy }, { checkedInByProfileAlias[UserProfilesTable.userId] })
            .leftJoin(UserTable.alias("checkedOutBy"), { AttendanceTable.checkedOutBy }, { UserTable.alias("checkedOutBy")[UserTable.id] })
            .leftJoin(checkedOutByProfileAlias, { AttendanceTable.checkedOutBy }, { checkedOutByProfileAlias[UserProfilesTable.userId] })
            .select {
                (AttendanceTable.eventType eq eventType.name) and
                (AttendanceTable.eventId eq eventId)
            }
            .map { row ->
                val attendance = row.toAttendance()
                
                // Get attendee name
                val attendeeName = when {
                    attendance.userId != null -> {
                        val firstName = row.getOrNull(UserProfilesTable.firstName)
                        val lastName = row.getOrNull(UserProfilesTable.lastName)
                        if (firstName != null && lastName != null) "$firstName $lastName" else "Unknown User"
                    }
                    attendance.kidId != null -> {
                        val firstName = row.getOrNull(KidTable.firstName)
                        val lastName = row.getOrNull(KidTable.lastName)
                        if (firstName != null && lastName != null) "$firstName $lastName" else "Unknown Kid"
                    }
                    else -> "Unknown Attendee"
                }
                
                // Get event name based on event type
                val eventName = when (eventType) {
                    EventType.EVENT -> {
                        val eventRow = EventTable.select { EventTable.id eq eventId }.firstOrNull()
                        eventRow?.get(EventTable.title) ?: "Unknown Event"
                    }
                    EventType.SERVICE -> {
                        val serviceRow = ServiceTable.select { ServiceTable.id.eq(eventId) }.firstOrNull()
                        serviceRow?.get(ServiceTable.name) ?: "Unknown Service"
                    }
                    EventType.KIDS_SERVICE -> {
                        val kidsServiceRow = KidsServiceTable.select { KidsServiceTable.id.eq(eventId) }.firstOrNull()
                        kidsServiceRow?.get(KidsServiceTable.name) ?: "Unknown Kids Service"
                    }
                }
                
                // Get checked in by name
                val checkedInByFirstName = row.getOrNull(checkedInByProfileAlias[UserProfilesTable.firstName])
                val checkedInByLastName = row.getOrNull(checkedInByProfileAlias[UserProfilesTable.lastName])
                val checkedInByName = if (checkedInByFirstName != null && checkedInByLastName != null) {
                    "$checkedInByFirstName $checkedInByLastName"
                } else "Unknown Staff"
                
                // Get checked out by name if applicable
                val checkedOutByName = if (attendance.checkedOutBy != null) {
                    val checkedOutByFirstName = row.getOrNull(checkedOutByProfileAlias[UserProfilesTable.firstName])
                    val checkedOutByLastName = row.getOrNull(checkedOutByProfileAlias[UserProfilesTable.lastName])
                    if (checkedOutByFirstName != null && checkedOutByLastName != null) {
                        "$checkedOutByFirstName $checkedOutByLastName"
                    } else "Unknown Staff"
                } else null
                
                AttendanceWithDetails(
                    attendance = attendance,
                    attendeeName = attendeeName,
                    eventName = eventName,
                    checkedInByName = checkedInByName,
                    checkedOutByName = checkedOutByName
                )
            }
        
        attendances
    }
    
    override suspend fun getAttendanceByUser(
        userId: Int,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?
    ): List<AttendanceWithDetails> = transaction {
        val checkedInByProfileAlias = UserProfilesTable.alias("checkedInByProfile")
        val checkedOutByProfileAlias = UserProfilesTable.alias("checkedOutByProfile")
        
        val query = AttendanceTable
            .leftJoin(UserTable, { AttendanceTable.userId }, { UserTable.id })
            .leftJoin(UserProfilesTable, { AttendanceTable.userId }, { UserProfilesTable.userId })
            .leftJoin(UserTable.alias("checkedInBy"), { AttendanceTable.checkedInBy }, { UserTable.alias("checkedInBy")[UserTable.id] })
            .leftJoin(checkedInByProfileAlias, { AttendanceTable.checkedInBy }, { checkedInByProfileAlias[UserProfilesTable.userId] })
            .leftJoin(UserTable.alias("checkedOutBy"), { AttendanceTable.checkedOutBy }, { UserTable.alias("checkedOutBy")[UserTable.id] })
            .leftJoin(checkedOutByProfileAlias, { AttendanceTable.checkedOutBy }, { checkedOutByProfileAlias[UserProfilesTable.userId] })
            .select { AttendanceTable.userId eq userId }
        
        val dateFilteredQuery = when {
            startDate != null && endDate != null -> query.andWhere { 
                (AttendanceTable.checkInTime greaterEq startDate) and 
                (AttendanceTable.checkInTime lessEq endDate) 
            }
            startDate != null -> query.andWhere { AttendanceTable.checkInTime greaterEq startDate }
            endDate != null -> query.andWhere { AttendanceTable.checkInTime lessEq endDate }
            else -> query
        }
        
        dateFilteredQuery.map { row ->
            val attendance = row.toAttendance()
            
            // Get attendee name
            val firstName = row.getOrNull(UserProfilesTable.firstName) ?: ""
            val lastName = row.getOrNull(UserProfilesTable.lastName) ?: ""
            val attendeeName = if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                "$firstName $lastName"
            } else "Unknown User"
            
            // Get event name based on event type
            val eventType = EventType.valueOf(row[AttendanceTable.eventType])
            val eventId = row[AttendanceTable.eventId]
            val eventName = when (eventType) {
                EventType.EVENT -> {
                    val eventRow = EventTable.select { EventTable.id eq eventId }.firstOrNull()
                    eventRow?.get(EventTable.title) ?: "Unknown Event"
                }
                EventType.SERVICE -> {
                    val serviceRow = ServiceTable.select { ServiceTable.id.eq(eventId) }.firstOrNull()
                    serviceRow?.get(ServiceTable.name) ?: "Unknown Service"
                }
                EventType.KIDS_SERVICE -> {
                    val kidsServiceRow = KidsServiceTable.select { KidsServiceTable.id.eq(eventId) }.firstOrNull()
                    kidsServiceRow?.get(KidsServiceTable.name) ?: "Unknown Kids Service"
                }
            }
            
            // Get checked in by name
            val checkedInByFirstName = row.getOrNull(checkedInByProfileAlias[UserProfilesTable.firstName]) ?: ""
            val checkedInByLastName = row.getOrNull(checkedInByProfileAlias[UserProfilesTable.lastName]) ?: ""
            val checkedInByName = if (checkedInByFirstName.isNotEmpty() && checkedInByLastName.isNotEmpty()) {
                "$checkedInByFirstName $checkedInByLastName"
            } else "Unknown Staff"
            
            // Get checked out by name if applicable
            val checkedOutByName = if (attendance.checkedOutBy != null) {
                val checkedOutByFirstName = row.getOrNull(checkedOutByProfileAlias[UserProfilesTable.firstName]) ?: ""
                val checkedOutByLastName = row.getOrNull(checkedOutByProfileAlias[UserProfilesTable.lastName]) ?: ""
                if (checkedOutByFirstName.isNotEmpty() && checkedOutByLastName.isNotEmpty()) {
                    "$checkedOutByFirstName $checkedOutByLastName"
                } else "Unknown Staff"
            } else null
            
            AttendanceWithDetails(
                attendance = attendance,
                attendeeName = attendeeName,
                eventName = eventName,
                checkedInByName = checkedInByName,
                checkedOutByName = checkedOutByName
            )
        }
    }
    
    override suspend fun getAttendanceByKid(
        kidId: Int,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?
    ): List<AttendanceWithDetails> = transaction {
        val checkedInByProfileAlias = UserProfilesTable.alias("checkedInByProfile")
        val checkedOutByProfileAlias = UserProfilesTable.alias("checkedOutByProfile")
        
        val query = AttendanceTable
            .leftJoin(KidTable, { AttendanceTable.kidId }, { KidTable.id })
            .leftJoin(UserTable.alias("checkedInBy"), { AttendanceTable.checkedInBy }, { UserTable.alias("checkedInBy")[UserTable.id] })
            .leftJoin(checkedInByProfileAlias, { AttendanceTable.checkedInBy }, { checkedInByProfileAlias[UserProfilesTable.userId] })
            .leftJoin(UserTable.alias("checkedOutBy"), { AttendanceTable.checkedOutBy }, { UserTable.alias("checkedOutBy")[UserTable.id] })
            .leftJoin(checkedOutByProfileAlias, { AttendanceTable.checkedOutBy }, { checkedOutByProfileAlias[UserProfilesTable.userId] })
            .select { AttendanceTable.kidId eq kidId }
        
        val dateFilteredQuery = when {
            startDate != null && endDate != null -> query.andWhere { 
                (AttendanceTable.checkInTime greaterEq startDate) and 
                (AttendanceTable.checkInTime lessEq endDate) 
            }
            startDate != null -> query.andWhere { AttendanceTable.checkInTime greaterEq startDate }
            endDate != null -> query.andWhere { AttendanceTable.checkInTime lessEq endDate }
            else -> query
        }
        
        dateFilteredQuery.map { row ->
            val attendance = row.toAttendance()
            
            // Get kid name
            val firstName = row.getOrNull(KidTable.firstName) ?: ""
            val lastName = row.getOrNull(KidTable.lastName) ?: ""
            val kidName = if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                "$firstName $lastName"
            } else "Unknown Kid"
            
            // Get event name based on event type
            val eventType = EventType.valueOf(row[AttendanceTable.eventType])
            val eventId = row[AttendanceTable.eventId]
            val eventName = when (eventType) {
                EventType.EVENT -> {
                    val eventRow = EventTable.select { EventTable.id eq eventId }.firstOrNull()
                    eventRow?.get(EventTable.title) ?: "Unknown Event"
                }
                EventType.SERVICE -> {
                    val serviceRow = ServiceTable.select { ServiceTable.id.eq(eventId) }.firstOrNull()
                    serviceRow?.get(ServiceTable.name) ?: "Unknown Service"
                }
                EventType.KIDS_SERVICE -> {
                    val kidsServiceRow = KidsServiceTable.select { KidsServiceTable.id.eq(eventId) }.firstOrNull()
                    kidsServiceRow?.get(KidsServiceTable.name) ?: "Unknown Kids Service"
                }
            }
            
            // Get checked in by name
            val checkedInByFirstName = row.getOrNull(checkedInByProfileAlias[UserProfilesTable.firstName]) ?: ""
            val checkedInByLastName = row.getOrNull(checkedInByProfileAlias[UserProfilesTable.lastName]) ?: ""
            val checkedInByName = if (checkedInByFirstName.isNotEmpty() && checkedInByLastName.isNotEmpty()) {
                "$checkedInByFirstName $checkedInByLastName"
            } else "Unknown Staff"
            
            // Get checked out by name if applicable
            val checkedOutByName = if (attendance.checkedOutBy != null) {
                val checkedOutByFirstName = row.getOrNull(checkedOutByProfileAlias[UserProfilesTable.firstName]) ?: ""
                val checkedOutByLastName = row.getOrNull(checkedOutByProfileAlias[UserProfilesTable.lastName]) ?: ""
                if (checkedOutByFirstName.isNotEmpty() && checkedOutByLastName.isNotEmpty()) {
                    "$checkedOutByFirstName $checkedOutByLastName"
                } else "Unknown Staff"
            } else null
            
            AttendanceWithDetails(
                attendance = attendance,
                attendeeName = kidName,
                eventName = eventName,
                checkedInByName = checkedInByName,
                checkedOutByName = checkedOutByName
            )
        }
    }
    
    override suspend fun getCurrentlyCheckedIn(
        eventType: EventType,
        eventId: Int
    ): List<AttendanceWithDetails> {
        val attendances = getAttendanceByEvent(eventType, eventId)
        return attendances.filter { it.attendance.status == AttendanceStatus.CHECKED_IN }
    }
    
    override suspend fun getAttendanceStats(
        eventType: EventType,
        eventId: Int
    ): AttendanceStats = transaction {
        val attendances = AttendanceTable.select {
            (AttendanceTable.eventType eq eventType.name) and
            (AttendanceTable.eventId eq eventId)
        }.toList()
        
        AttendanceStats(
            totalAttendees = attendances.size,
            currentlyCheckedIn = attendances.count { it[AttendanceTable.status] == AttendanceStatus.CHECKED_IN.name },
            checkedOut = attendances.count { it[AttendanceTable.status] == AttendanceStatus.CHECKED_OUT.name },
            noShows = attendances.count { it[AttendanceTable.status] == AttendanceStatus.NO_SHOW.name },
            emergencies = attendances.count { it[AttendanceTable.status] == AttendanceStatus.EMERGENCY.name }
        )
    }
    
    override suspend fun isUserCheckedIn(
        eventType: EventType,
        eventId: Int,
        userId: Int
    ): Boolean = transaction {
        AttendanceTable.select {
            (AttendanceTable.eventType eq eventType.name) and
            (AttendanceTable.eventId eq eventId) and
            (AttendanceTable.userId eq userId) and
            (AttendanceTable.status eq AttendanceStatus.CHECKED_IN.name)
        }.count() > 0
    }
    
    override suspend fun isKidCheckedIn(
        eventType: EventType,
        eventId: Int,
        kidId: Int
    ): Boolean = transaction {
        AttendanceTable.select {
            (AttendanceTable.eventType eq eventType.name) and
            (AttendanceTable.eventId eq eventId) and
            (AttendanceTable.kidId eq kidId) and
            (AttendanceTable.status eq AttendanceStatus.CHECKED_IN.name)
        }.count() > 0
    }
    
    override suspend fun updateAttendanceStatus(
        attendanceId: Int,
        status: AttendanceStatus,
        notes: String
    ): Boolean = transaction {
        val attendance = AttendanceTable.select { AttendanceTable.id eq attendanceId }.firstOrNull()
            ?: return@transaction false
        
        val updatedRows = AttendanceTable.update({ AttendanceTable.id eq attendanceId }) {
            it[AttendanceTable.status] = status.name
            if (notes.isNotEmpty()) {
                it[AttendanceTable.notes] = attendance[AttendanceTable.notes] + "\nStatus update: $notes"
            }
        }
        
        updatedRows > 0
    }
    
    override suspend fun updateAttendanceNotes(
        attendanceId: Int,
        notes: String
    ): Boolean = transaction {
        val updatedRows = AttendanceTable.update({ AttendanceTable.id eq attendanceId }) {
            it[AttendanceTable.notes] = notes
        }
        
        updatedRows > 0
    }
}