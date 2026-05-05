package rfm.com.controller

import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import rfm.com.dto.*
import rfm.com.entity.MembershipStatus
import rfm.com.entity.MinistryRole
import rfm.com.entity.MinistryType
import rfm.com.service.MinistryMembershipService
import rfm.com.service.MinistryService
import rfm.com.util.getCurrentUserId

/**
 * Public + member + leader-scoped ministry endpoints. Admin-only writes
 * live in [rfm.com.controller.admin.AdminMinistryController].
 */
@RestController
@RequestMapping("/api/ministries")
class MinistryController(
    private val ministryService: MinistryService,
    private val membershipService: MinistryMembershipService
) {

    @GetMapping("/types")
    fun listTypes(): ResponseEntity<ApiResponse<List<MinistryTypeOption>>> {
        val options = MinistryType.values().map { MinistryTypeOption(it, englishLabel(it), portugueseLabel(it)) }
        return ResponseEntity.ok(ApiResponse(success = true, message = "Ministry types retrieved", data = options))
    }

    @GetMapping
    fun listMinistries(
        @RequestParam(required = false) type: MinistryType?,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false, name = "q") query: String?,
        @RequestParam(defaultValue = "false") joinableOnly: Boolean,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "name") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDir: String
    ): ResponseEntity<ApiResponse<Page<MinistrySummaryResponse>>> = runBlocking {
        val sort = if (sortDir.equals("desc", true)) Sort.by(sortBy).descending() else Sort.by(sortBy).ascending()
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val results = ministryService.listMinistries(type, city, query, joinableOnly, includeInactive = false, pageable = pageable)
        ResponseEntity.ok(ApiResponse(success = true, message = "Ministries retrieved", data = results))
    }

    @GetMapping("/{id}")
    fun getMinistry(@PathVariable id: String): ResponseEntity<ApiResponse<MinistryResponse>> = runBlocking {
        val ministry = ministryService.getById(id)
        ResponseEntity.ok(ApiResponse(success = true, message = "Ministry retrieved", data = ministry))
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun listMyMinistries(authentication: Authentication): ResponseEntity<ApiResponse<List<MyMinistryResponse>>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        val list = membershipService.listMyMinistries(userId)
        ResponseEntity.ok(ApiResponse(success = true, message = "My ministries retrieved", data = list))
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("isAuthenticated()")
    fun joinMinistry(
        @PathVariable id: String,
        @Valid @RequestBody(required = false) request: JoinMinistryRequest?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<MembershipResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        val membership = membershipService.selfJoin(userId, id, request?.message)
        ResponseEntity.ok(ApiResponse(success = true, message = "Join request created", data = membership))
    }

    @DeleteMapping("/{id}/leave")
    @PreAuthorize("isAuthenticated()")
    fun leaveMinistry(@PathVariable id: String, authentication: Authentication): ResponseEntity<ApiResponse<String>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        membershipService.leave(userId, id)
        ResponseEntity.ok(ApiResponse(success = true, message = "Left ministry", data = id))
    }

    // ----------------- Leader endpoints -----------------

    @GetMapping("/{id}/members")
    @PreAuthorize("@ministryPerms.canManageMinistry(authentication, #id) or hasAnyRole('STAFF','ADMIN')")
    fun listMembers(
        @PathVariable id: String,
        @RequestParam(required = false) status: MembershipStatus?,
        @RequestParam(required = false) role: MinistryRole?,
        @RequestParam(required = false, name = "q") query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<ApiResponse<Page<MemberResponse>>> = runBlocking {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by("joinedAt").descending())
        val members = membershipService.listMembers(id, status, role, query, pageable)
        ResponseEntity.ok(ApiResponse(success = true, message = "Members retrieved", data = members))
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("@ministryPerms.canManageMinistry(authentication, #id)")
    fun addMember(
        @PathVariable id: String,
        @Valid @RequestBody request: AddMemberRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<MemberResponse>> = runBlocking {
        val by = authentication.getCurrentUserId()
        val member = membershipService.addMember(id, request, by)
        ResponseEntity.ok(ApiResponse(success = true, message = "Member added", data = member))
    }

    @PutMapping("/{id}/members/{userId}")
    @PreAuthorize("@ministryPerms.canManageMinistry(authentication, #id)")
    fun updateMember(
        @PathVariable id: String,
        @PathVariable userId: String,
        @Valid @RequestBody request: UpdateMemberRequest
    ): ResponseEntity<ApiResponse<MemberResponse>> = runBlocking {
        val member = membershipService.updateMember(id, userId, request)
        ResponseEntity.ok(ApiResponse(success = true, message = "Member updated", data = member))
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("@ministryPerms.canManageMinistry(authentication, #id)")
    fun removeMember(
        @PathVariable id: String,
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<String>> = runBlocking {
        membershipService.removeMember(id, userId)
        ResponseEntity.ok(ApiResponse(success = true, message = "Member removed", data = userId))
    }

    @PostMapping("/{id}/members/{userId}/approve")
    @PreAuthorize("@ministryPerms.canManageMinistry(authentication, #id)")
    fun approveMember(
        @PathVariable id: String,
        @PathVariable userId: String,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<MemberResponse>> = runBlocking {
        val approver = authentication.getCurrentUserId()
        val member = membershipService.approve(id, userId, approver)
        ResponseEntity.ok(ApiResponse(success = true, message = "Member approved", data = member))
    }

    @PostMapping("/{id}/members/{userId}/reject")
    @PreAuthorize("@ministryPerms.canManageMinistry(authentication, #id)")
    fun rejectMember(
        @PathVariable id: String,
        @PathVariable userId: String,
        @Valid @RequestBody(required = false) request: RejectMemberRequest?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<MemberResponse>> = runBlocking {
        val by = authentication.getCurrentUserId()
        val member = membershipService.reject(id, userId, request?.reason, by)
        ResponseEntity.ok(ApiResponse(success = true, message = "Member rejected", data = member))
    }

    private fun englishLabel(m: MinistryType): String = when (m) {
        MinistryType.SISTERHOOD -> "Sisterhood"
        MinistryType.JOVENS_YXYA -> "Young Adults (YxYa)"
        MinistryType.MENS -> "Men"
        MinistryType.CASAIS -> "Couples"
        MinistryType.THIRTY_PLUS -> "30+"
        MinistryType.GERAL -> "General"
        MinistryType.WORSHIP -> "Worship"
        MinistryType.KIDS -> "Kids"
        MinistryType.USHERS -> "Ushers"
        MinistryType.HOSPITALITY -> "Hospitality"
        MinistryType.MEDIA -> "Media"
        MinistryType.PRODUCTION -> "Production"
        MinistryType.CONNECT -> "Connect"
        MinistryType.CAFE -> "Cafe"
    }

    private fun portugueseLabel(m: MinistryType): String = when (m) {
        MinistryType.SISTERHOOD -> "Sisterhood"
        MinistryType.JOVENS_YXYA -> "Jovens YxYa"
        MinistryType.MENS -> "Homens"
        MinistryType.CASAIS -> "Casais"
        MinistryType.THIRTY_PLUS -> "30+"
        MinistryType.GERAL -> "Geral"
        MinistryType.WORSHIP -> "Louvor"
        MinistryType.KIDS -> "Kids"
        MinistryType.USHERS -> "Recepção"
        MinistryType.HOSPITALITY -> "Hospitalidade"
        MinistryType.MEDIA -> "Multimédia"
        MinistryType.PRODUCTION -> "Produção"
        MinistryType.CONNECT -> "Connect"
        MinistryType.CAFE -> "Café"
    }
}
