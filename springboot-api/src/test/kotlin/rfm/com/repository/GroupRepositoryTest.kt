package rfm.com.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import rfm.com.entity.Group
import rfm.com.entity.GroupLocation
import rfm.com.entity.Ministry
import java.time.DayOfWeek
import java.time.LocalTime

class GroupRepositoryTest : BaseMongoRepositoryTest() {

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @BeforeEach
    fun setUp() {
        groupRepository.deleteAll()
        groupRepository.saveAll(
            listOf(
                group("Sisterhood Lisboa", Ministry.SISTERHOOD, "Lisboa", 38.7101, -9.1414),
                group("Jovens YxYa Lisboa", Ministry.JOVENS_YXYA, "Lisboa", 38.7195, -9.1643),
                group("Homens Porto", Ministry.MENS, "Porto", 41.1496, -8.6075),
                group("Casais Cascais", Ministry.CASAIS, "Cascais", 38.6979, -9.4215),
                // inactive group — should be excluded from every public query
                group(
                    "Inactive Group",
                    Ministry.GERAL,
                    "Lisboa",
                    38.72,
                    -9.14,
                    isActive = false
                )
            )
        )
    }

    @Test
    fun `findByIsActiveTrue skips inactive groups`() {
        val page = groupRepository.findByIsActiveTrue(PageRequest.of(0, 10))
        assertEquals(4, page.totalElements)
        assertTrue(page.content.none { it.name == "Inactive Group" })
    }

    @Test
    fun `findByIsActiveTrueAndMinistry filters by ministry`() {
        val page = groupRepository.findByIsActiveTrueAndMinistry(
            Ministry.SISTERHOOD,
            PageRequest.of(0, 10)
        )
        assertEquals(1, page.totalElements)
        assertEquals("Sisterhood Lisboa", page.content.first().name)
    }

    @Test
    fun `findByIsActiveTrueAndLocationCity filters by city`() {
        val page = groupRepository.findByIsActiveTrueAndLocationCity("Lisboa", PageRequest.of(0, 10))
        assertEquals(2, page.totalElements)
        assertTrue(page.content.all { it.location.city == "Lisboa" })
    }

    @Test
    fun `findByIsActiveTrueAndMinistryAndLocationCity combines filters`() {
        val page = groupRepository.findByIsActiveTrueAndMinistryAndLocationCity(
            Ministry.JOVENS_YXYA,
            "Lisboa",
            PageRequest.of(0, 10)
        )
        assertEquals(1, page.totalElements)
        assertEquals("Jovens YxYa Lisboa", page.content.first().name)
    }

    @Test
    fun `findActiveNear returns groups within radius ordered by distance`() {
        // Search point near Garrett in Lisboa — Sisterhood is ~0m, Jovens YxYa ~2km, Porto is ~300km
        val results = groupRepository.findActiveNear(
            longitude = -9.1414,
            latitude = 38.7101,
            maxDistanceMeters = 10_000.0
        )
        val names = results.map { it.name }
        assertEquals(listOf("Sisterhood Lisboa", "Jovens YxYa Lisboa"), names)
    }

    @Test
    fun `findActiveNear excludes inactive groups`() {
        val results = groupRepository.findActiveNear(
            longitude = -9.14,
            latitude = 38.72,
            maxDistanceMeters = 5_000.0
        )
        assertTrue(results.none { it.name == "Inactive Group" })
    }

    @Test
    fun `findActiveNearByMinistry filters by ministry and proximity`() {
        val results = groupRepository.findActiveNearByMinistry(
            Ministry.SISTERHOOD,
            longitude = -9.1414,
            latitude = 38.7101,
            maxDistanceMeters = 10_000.0
        )
        assertEquals(1, results.size)
        assertEquals("Sisterhood Lisboa", results.first().name)
    }

    private fun group(
        name: String,
        ministry: Ministry,
        city: String,
        lat: Double,
        lng: Double,
        isActive: Boolean = true
    ) = Group(
        name = name,
        ministry = ministry,
        description = "desc",
        leaderName = "Leader",
        leaderContact = "+351000000000",
        meetingDay = DayOfWeek.THURSDAY,
        meetingTime = LocalTime.of(20, 0),
        location = GroupLocation(
            addressLine = "Rua Teste",
            city = city,
            region = null,
            postalCode = null,
            country = "PT",
            coordinates = doubleArrayOf(lng, lat)
        ),
        isActive = isActive
    )
}
