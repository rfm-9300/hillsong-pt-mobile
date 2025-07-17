package rfm.com.plugins

import rfm.com.data.db.event.EventRepository
import rfm.com.data.db.post.PostRepository
import rfm.com.data.db.user.UserRepository
import rfm.com.data.db.service.ServiceRepository
import rfm.com.data.db.kidsservice.KidsServiceRepository
import rfm.com.routes.*
import rfm.com.security.hashing.HashingService
import rfm.com.security.token.TokenConfig
import rfm.com.security.token.TokenService
import rfm.com.services.AttendanceService
import rfm.com.services.EmailService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import rfm.com.data.db.kid.KidRepository


fun Application.configureRouting(
    userRepository: UserRepository,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    eventRepository: EventRepository,
    postRepository: PostRepository,
    emailService: EmailService,
    kidRepository: KidRepository,
    serviceRepository: ServiceRepository,
    kidsServiceRepository: KidsServiceRepository,
    attendanceService: AttendanceService
)  {
    routing {
        postRoutes( eventRepository, userRepository, postRepository )
        loginRoutes(hashingService, userRepository, tokenService, tokenConfig, emailService)
        eventRoutes(eventRepository, userRepository)
        dynamicJsProcessing()
        profileRoutes(userRepository)
        userRoutes(userRepository)
        kidRoutes(kidRepository)
        serviceRoutes(serviceRepository)
        kidsServiceRoutes(kidsServiceRepository)
        attendanceRoutes(attendanceService)
    }
}
