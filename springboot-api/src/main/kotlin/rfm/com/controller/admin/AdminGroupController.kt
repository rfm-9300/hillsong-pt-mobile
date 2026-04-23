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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.ApiResponse
import rfm.com.dto.CreateGroupRequest
import rfm.com.dto.GroupResponse
import rfm.com.dto.GroupSummaryResponse
import rfm.com.dto.UpdateGroupRequest
import rfm.com.entity.Ministry
import rfm.com.service.GroupService

@RestController
@RequestMapping("/api/admin/groups")
@PreAuthorize("hasRole('ADMIN')")
class AdminGroupController(
    private val groupService: GroupService
) {

    private val logger = LoggerFactory.getLogger(AdminGroupController::class.java)

    @GetMapping
    fun listGroups(
        @RequestParam(required = false) ministry: Ministry?,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false, name = "q") query: String?,
        @RequestParam(defaultValue = "true") includeInactive: Boolean,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        @RequestParam(defaultValue = "updatedAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String
    ): ResponseEntity<ApiResponse<Page<GroupSummaryResponse>>> = runBlocking {
        val sort = if (sortDir.equals("desc", ignoreCase = true)) {
            Sort.by(sortBy).descending()
        } else {
            Sort.by(sortBy).ascending()
        }
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val groups = groupService.listAdminGroups(ministry, city, query, includeInactive, pageable)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Admin groups retrieved successfully",
                data = groups
            )
        )
    }

    @GetMapping("/{id}")
    fun getGroupById(@PathVariable id: String): ResponseEntity<ApiResponse<GroupResponse>> = runBlocking {
        val group = groupService.getById(id, includeInactive = true)
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Group retrieved successfully",
                data = group
            )
        )
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createGroup(
        @Valid @RequestPart("group") request: CreateGroupRequest,
        @RequestPart("image", required = false) image: MultipartFile?
    ): ResponseEntity<ApiResponse<GroupResponse>> = runBlocking {
        logger.info("Creating group: ${request.name}")
        val group = groupService.create(request, image)
        ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(success = true, message = "Group created successfully", data = group)
        )
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateGroup(
        @PathVariable id: String,
        @Valid @RequestPart("group") request: UpdateGroupRequest,
        @RequestPart("image", required = false) image: MultipartFile?
    ): ResponseEntity<ApiResponse<GroupResponse>> = runBlocking {
        logger.info("Updating group: $id")
        val group = groupService.update(id, request, image)
        ResponseEntity.ok(
            ApiResponse(success = true, message = "Group updated successfully", data = group)
        )
    }

    @DeleteMapping("/{id}")
    fun deleteGroup(@PathVariable id: String): ResponseEntity<ApiResponse<String>> = runBlocking {
        logger.info("Soft-deleting group: $id")
        groupService.softDelete(id)
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Group deleted successfully",
                data = "Group with id $id has been deactivated"
            )
        )
    }
}
