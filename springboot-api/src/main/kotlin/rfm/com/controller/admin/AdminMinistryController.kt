package rfm.com.controller.admin

import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.entity.MinistryType
import rfm.com.service.MinistryMembershipService
import rfm.com.service.MinistryService
import rfm.com.util.getCurrentUserId

@RestController
@RequestMapping("/api/admin/ministries")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
class AdminMinistryController(
    private val ministryService: MinistryService,
    private val membershipService: MinistryMembershipService
) {

    private val logger = LoggerFactory.getLogger(AdminMinistryController::class.java)

    @GetMapping
    fun listMinistries(
        @RequestParam(required = false) type: MinistryType?,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false, name = "q") query: String?,
        @RequestParam(defaultValue = "true") includeInactive: Boolean,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        @RequestParam(defaultValue = "updatedAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String
    ): ResponseEntity<ApiResponse<Page<MinistrySummaryResponse>>> = runBlocking {
        val sort = if (sortDir.equals("desc", true)) Sort.by(sortBy).descending() else Sort.by(sortBy).ascending()
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val results = ministryService.listMinistries(type, city, query, joinableOnly = false, includeInactive = includeInactive, pageable = pageable)
        ResponseEntity.ok(ApiResponse(success = true, message = "Admin ministries retrieved", data = results))
    }

    @GetMapping("/{id}")
    fun getMinistry(@PathVariable id: String): ResponseEntity<ApiResponse<MinistryResponse>> = runBlocking {
        val ministry = ministryService.getById(id, includeInactive = true)
        ResponseEntity.ok(ApiResponse(success = true, message = "Ministry retrieved", data = ministry))
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    fun createMinistry(
        @Valid @RequestPart("ministry") request: CreateMinistryRequest,
        @RequestPart("image", required = false) image: MultipartFile?
    ): ResponseEntity<ApiResponse<MinistryResponse>> = runBlocking {
        logger.info("Creating ministry: ${request.name}")
        val ministry = ministryService.create(request, image)
        ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(success = true, message = "Ministry created", data = ministry)
        )
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    fun updateMinistry(
        @PathVariable id: String,
        @Valid @RequestPart("ministry") request: UpdateMinistryRequest,
        @RequestPart("image", required = false) image: MultipartFile?
    ): ResponseEntity<ApiResponse<MinistryResponse>> = runBlocking {
        val ministry = ministryService.update(id, request, image)
        ResponseEntity.ok(ApiResponse(success = true, message = "Ministry updated", data = ministry))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteMinistry(@PathVariable id: String): ResponseEntity<ApiResponse<String>> = runBlocking {
        ministryService.softDelete(id)
        ResponseEntity.ok(ApiResponse(success = true, message = "Ministry deactivated", data = id))
    }

    @PostMapping("/{id}/leaders")
    @PreAuthorize("hasRole('ADMIN')")
    fun assignLeader(
        @PathVariable id: String,
        @Valid @RequestBody request: AssignLeaderRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<MemberResponse>> = runBlocking {
        val by = authentication.getCurrentUserId()
        val member = membershipService.assignLeader(id, request, by)
        ResponseEntity.ok(ApiResponse(success = true, message = "Leader assigned", data = member))
    }

    @DeleteMapping("/{id}/leaders/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun revokeLeader(
        @PathVariable id: String,
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<MemberResponse>> = runBlocking {
        val member = membershipService.revokeLeader(id, userId)
        ResponseEntity.ok(ApiResponse(success = true, message = "Leader revoked", data = member))
    }
}
