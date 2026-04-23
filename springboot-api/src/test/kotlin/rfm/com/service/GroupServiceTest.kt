package rfm.com.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.CreateGroupRequest
import rfm.com.dto.GroupLocationDto
import rfm.com.entity.Group
import rfm.com.entity.GroupLocation
import rfm.com.entity.MeetingFrequency
import rfm.com.entity.Ministry
import rfm.com.repository.GroupRepository
import java.time.DayOfWeek
import java.time.LocalTime

class GroupServiceTest {

    private lateinit var groupRepository: GroupRepository
    private lateinit var fileStorageService: FileStorageService
    private lateinit var groupService: GroupService

    @BeforeEach
    fun setUp() {
        groupRepository = mockk()
        fileStorageService = mockk()
        groupService = GroupService(groupRepository, fileStorageService)
    }

    @Test
    fun `create stores image in groups prefix and maps dto`() = runBlocking {
        val image = mockk<MultipartFile>()
        val request = CreateGroupRequest(
            name = "Sisterhood Lisboa",
            ministry = Ministry.SISTERHOOD,
            description = "Weekly connection group",
            leaderName = "Ana Silva",
            leaderContact = "+351912345678",
            meetingDay = DayOfWeek.THURSDAY,
            meetingTime = LocalTime.of(19, 30),
            frequency = MeetingFrequency.WEEKLY,
            location = GroupLocationDto(
                addressLine = "Rua Garrett 50",
                city = "Lisboa",
                region = "Lisboa",
                postalCode = "1200-203",
                country = "PT",
                latitude = 38.7101,
                longitude = -9.1414
            ),
            maxMembers = 24,
            currentMembers = 8,
            isActive = true,
            isJoinable = true,
            tags = listOf("women", "downtown")
        )

        every { fileStorageService.storeGroupImage(image) } returns "groups/group-image.jpg"
        every { groupRepository.save(any()) } answers { firstArg<Group>().copy(id = "group-1") }

        val created = groupService.create(request, image)

        assertEquals("group-1", created.id)
        assertEquals("groups/group-image.jpg", created.imagePath)
        assertEquals("Lisboa", created.location.city)
        assertEquals(38.7101, created.location.latitude)
        assertEquals(-9.1414, created.location.longitude)
        assertEquals(listOf("women", "downtown"), created.tags)

        verify(exactly = 1) { fileStorageService.storeGroupImage(image) }
        verify(exactly = 1) { groupRepository.save(any()) }
    }

    @Test
    fun `listGroups applies query filter for geo results`() = runBlocking {
        every {
            groupRepository.findActiveNear(any(), any(), any())
        } returns listOf(
            group(name = "Sisterhood Lisboa", description = "Women group"),
            group(name = "Casais Cascais", description = "Couples group")
        )

        val page = groupService.listGroups(
            ministry = null,
            city = null,
            latitude = 38.7101,
            longitude = -9.1414,
            radiusKm = 15.0,
            query = "women",
            pageable = PageRequest.of(0, 20)
        )

        assertEquals(1, page.totalElements)
        assertEquals("Sisterhood Lisboa", page.content.first().name)
        assertTrue(page.content.first().isActive)
        assertFalse(page.content.first().description.contains("Couples", ignoreCase = true))
    }

    private fun group(name: String, description: String) = Group(
        id = "group-${name.hashCode()}",
        name = name,
        ministry = Ministry.GERAL,
        description = description,
        leaderName = "Leader",
        leaderContact = "+351000000000",
        meetingDay = DayOfWeek.THURSDAY,
        meetingTime = LocalTime.of(20, 0),
        frequency = MeetingFrequency.WEEKLY,
        location = GroupLocation(
            addressLine = "Rua Teste",
            city = "Lisboa",
            region = "Lisboa",
            postalCode = "1000-000",
            country = "PT",
            coordinates = doubleArrayOf(-9.1414, 38.7101)
        ),
        imagePath = null,
        maxMembers = 12,
        currentMembers = 4,
        isActive = true,
        isJoinable = true,
        tags = listOf("community")
    )
}
