package rfm.com.controller

import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rfm.com.dto.*
import rfm.com.entity.MinistryType
import rfm.com.service.GroupService

/**
 * Connection groups. Read endpoints are public for the mobile app;
 * writes require an ADMIN role and go through the admin panel.
 */
@RestController
@RequestMapping("/api/groups")
class GroupController(
    private val groupService: GroupService
) {

    @GetMapping
    fun listGroups(
        @RequestParam(required = false) ministry: MinistryType?,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lng: Double?,
        @RequestParam(required = false) radiusKm: Double?,
        @RequestParam(required = false, name = "q") query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "name") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDir: String
    ): ResponseEntity<ApiResponse<Page<GroupSummaryResponse>>> = runBlocking {
        val sort = if (sortDir.equals("desc", ignoreCase = true))
            Sort.by(sortBy).descending() else Sort.by(sortBy).ascending()
        val pageable: Pageable = PageRequest.of(page, size, sort)

        val groups = groupService.listGroups(ministry, city, lat, lng, radiusKm, query, pageable)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Groups retrieved successfully",
                data = groups
            )
        )
    }

    @GetMapping("/ministries")
    fun listMinistries(): ResponseEntity<ApiResponse<List<MinistryOption>>> {
        val options = MinistryType.values().map { MinistryOption(it, englishLabel(it), portugueseLabel(it)) }
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Ministries retrieved successfully",
                data = options
            )
        )
    }

    @GetMapping("/{id}")
    fun getGroupById(@PathVariable id: String): ResponseEntity<ApiResponse<GroupResponse>> = runBlocking {
        val group = groupService.getById(id)
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Group retrieved successfully",
                data = group
            )
        )
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
