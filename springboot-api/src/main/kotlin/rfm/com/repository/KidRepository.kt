package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.Gender
import rfm.com.entity.Kid
import java.time.LocalDate

@Repository
interface KidRepository : MongoRepository<Kid, String> {
    
    fun findByIsActiveTrue(): List<Kid>
    
    fun findByIsActiveFalse(): List<Kid>
    
    fun findByPrimaryParentId(parentId: String): List<Kid>
    
    fun findBySecondaryParentId(parentId: String): List<Kid>
    
    @Query("{\$or: [{'primaryParentId': ?0}, {'secondaryParentId': ?0}]}")
    fun findByEitherParent(parentId: String): List<Kid>

    @Query("{\$or: [{'primaryParentId': ?0}, {'secondaryParentId': ?0}]}")
    fun findByEitherParent(parentId: String, pageable: Pageable): Page<Kid>
    
    @Query("{'firstName': {'\$regex': ?0, '\$options': 'i'}}")
    fun findByFirstNameContainingIgnoreCase(firstName: String): List<Kid>
    
    @Query("{'lastName': {'\$regex': ?0, '\$options': 'i'}}")
    fun findByLastNameContainingIgnoreCase(lastName: String): List<Kid>
    
    fun findByGender(gender: Gender): List<Kid>
    
    @Query("{'dateOfBirth': {'\$gte': ?0, '\$lte': ?1}}")
    fun findByDateOfBirthBetween(startDate: LocalDate, endDate: LocalDate): List<Kid>

    @Query("{'medicalNotes': {'\$ne': null, '\$ne': ''}}")
    fun findKidsWithMedicalNotes(): List<Kid>
    
    @Query("{'allergies': {'\$ne': null, '\$ne': ''}}")
    fun findKidsWithAllergies(): List<Kid>
    
    @Query("{'specialNeeds': {'\$ne': null, '\$ne': ''}}")
    fun findKidsWithSpecialNeeds(): List<Kid>
    
    fun countByPrimaryParentId(parentId: String): Long
    
    @Query(value = "{\$or: [{'primaryParentId': ?0}, {'secondaryParentId': ?0}]}", count = true)
    fun countByEitherParent(parentId: String): Long
    
    fun countByIsActiveTrue(): Long
    
    fun countByGender(gender: Gender): Long
    
    fun findByIsActiveTrue(pageable: Pageable): Page<Kid>

    fun existsByFirstNameAndLastNameAndDateOfBirth(firstName: String, lastName: String, dateOfBirth: LocalDate): Boolean
}