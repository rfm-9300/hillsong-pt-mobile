package rfm.com.config

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import rfm.com.entity.Group
import rfm.com.entity.GroupLocation
import rfm.com.entity.MeetingFrequency
import rfm.com.entity.Ministry
import rfm.com.repository.GroupRepository
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Inserts a handful of realistic connection groups on startup so the
 * mobile app and admin panel have something to render before content
 * is entered. Enabled only when `app.groups.seed.enabled=true`; runs
 * once and short-circuits if the collection is already populated.
 */
@Component
@ConditionalOnProperty(name = ["app.groups.seed.enabled"], havingValue = "true")
class GroupSeeder(
    private val groupRepository: GroupRepository
) {

    private val logger = LoggerFactory.getLogger(GroupSeeder::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun seed() {
        if (groupRepository.count() > 0) {
            logger.info("Groups collection already seeded, skipping")
            return
        }

        val groups = listOf(
            group(
                name = "Sisterhood Lisboa Centro",
                ministry = Ministry.SISTERHOOD,
                description = "Grupo de mulheres que se reúne semanalmente para crescer juntas na fé.",
                leaderName = "Ana Silva",
                leaderContact = "+351912345678",
                day = DayOfWeek.THURSDAY,
                time = LocalTime.of(19, 30),
                city = "Lisboa",
                region = "Lisboa",
                address = "Rua Garrett 50",
                postal = "1200-203",
                lat = 38.7101, lng = -9.1414
            ),
            group(
                name = "Jovens YxYa — Campo de Ourique",
                ministry = Ministry.JOVENS_YXYA,
                description = "Conexão semanal dos jovens adultos YxYa. Louvor, Palavra e convívio.",
                leaderName = "Pedro Santos",
                leaderContact = "+351913000111",
                day = DayOfWeek.FRIDAY,
                time = LocalTime.of(20, 0),
                city = "Lisboa",
                region = "Lisboa",
                address = "Rua Silva Carvalho 120",
                postal = "1250-251",
                lat = 38.7195, lng = -9.1643
            ),
            group(
                name = "Homens de Impacto Porto",
                ministry = Ministry.MENS,
                description = "Homens reunidos para orar, partilhar e servir.",
                leaderName = "João Ferreira",
                leaderContact = "+351914222333",
                day = DayOfWeek.TUESDAY,
                time = LocalTime.of(20, 30),
                city = "Porto",
                region = "Porto",
                address = "Rua de Santa Catarina 300",
                postal = "4000-445",
                lat = 41.1496, lng = -8.6075
            ),
            group(
                name = "Casais Cascais",
                ministry = Ministry.CASAIS,
                description = "Um espaço seguro para casais crescerem na relação e na fé.",
                leaderName = "Miguel & Rita Costa",
                leaderContact = "+351915444555",
                day = DayOfWeek.SATURDAY,
                time = LocalTime.of(18, 0),
                frequency = MeetingFrequency.BIWEEKLY,
                city = "Cascais",
                region = "Lisboa",
                address = "Avenida Marginal 5000",
                postal = "2750-427",
                lat = 38.6979, lng = -9.4215
            ),
            group(
                name = "30+ Lisboa",
                ministry = Ministry.THIRTY_PLUS,
                description = "Comunidade para os 30 e tais à procura de pertença e propósito.",
                leaderName = "Sara Mendes",
                leaderContact = "+351916666777",
                day = DayOfWeek.WEDNESDAY,
                time = LocalTime.of(20, 0),
                city = "Lisboa",
                region = "Lisboa",
                address = "Avenida da Liberdade 200",
                postal = "1250-147",
                lat = 38.7205, lng = -9.1458
            ),
            group(
                name = "Geral Almada",
                ministry = Ministry.GERAL,
                description = "Aberto a todos — um ponto de partida para se ligar à igreja.",
                leaderName = "Bruno Carvalho",
                leaderContact = "+351917888999",
                day = DayOfWeek.THURSDAY,
                time = LocalTime.of(20, 0),
                city = "Almada",
                region = "Setúbal",
                address = "Rua Cândido dos Reis 80",
                postal = "2800-276",
                lat = 38.6797, lng = -9.1569
            ),
            group(
                name = "Sisterhood Porto",
                ministry = Ministry.SISTERHOOD,
                description = "Mulheres a caminharem juntas no Porto.",
                leaderName = "Inês Lopes",
                leaderContact = "+351918101112",
                day = DayOfWeek.MONDAY,
                time = LocalTime.of(19, 0),
                city = "Porto",
                region = "Porto",
                address = "Rua de Cedofeita 150",
                postal = "4050-178",
                lat = 41.1557, lng = -8.6225
            ),
            group(
                name = "Jovens YxYa Porto",
                ministry = Ministry.JOVENS_YXYA,
                description = "YxYa no Porto. Sextas à noite, todos bem-vindos.",
                leaderName = "Tiago Monteiro",
                leaderContact = "+351919131415",
                day = DayOfWeek.FRIDAY,
                time = LocalTime.of(20, 30),
                city = "Porto",
                region = "Porto",
                address = "Rua do Almada 400",
                postal = "4050-036",
                lat = 41.1498, lng = -8.6149
            ),
            group(
                name = "Casais Lisboa Norte",
                ministry = Ministry.CASAIS,
                description = "Casais da zona norte de Lisboa. Jantar + partilha.",
                leaderName = "Ricardo & Marta Nunes",
                leaderContact = "+351920161718",
                day = DayOfWeek.FRIDAY,
                time = LocalTime.of(20, 0),
                frequency = MeetingFrequency.BIWEEKLY,
                city = "Lisboa",
                region = "Lisboa",
                address = "Avenida de Berna 25",
                postal = "1050-036",
                lat = 38.7369, lng = -9.1524
            ),
            group(
                name = "Geral Braga",
                ministry = Ministry.GERAL,
                description = "A crescer juntos em Braga.",
                leaderName = "Daniel Oliveira",
                leaderContact = "+351921192021",
                day = DayOfWeek.WEDNESDAY,
                time = LocalTime.of(20, 30),
                city = "Braga",
                region = "Braga",
                address = "Avenida Central 90",
                postal = "4710-228",
                lat = 41.5454, lng = -8.4265
            )
        )

        groupRepository.saveAll(groups)
        logger.info("Seeded ${groups.size} connection groups")
    }

    private fun group(
        name: String,
        ministry: Ministry,
        description: String,
        leaderName: String,
        leaderContact: String,
        day: DayOfWeek,
        time: LocalTime,
        frequency: MeetingFrequency = MeetingFrequency.WEEKLY,
        city: String,
        region: String,
        address: String,
        postal: String,
        lat: Double,
        lng: Double
    ) = Group(
        name = name,
        ministry = ministry,
        description = description,
        leaderName = leaderName,
        leaderContact = leaderContact,
        meetingDay = day,
        meetingTime = time,
        frequency = frequency,
        location = GroupLocation(
            addressLine = address,
            city = city,
            region = region,
            postalCode = postal,
            country = "PT",
            coordinates = doubleArrayOf(lng, lat)
        )
    )
}
