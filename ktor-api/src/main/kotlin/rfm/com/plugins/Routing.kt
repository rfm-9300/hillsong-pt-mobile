package rfm.com.plugins

import rfm.com.data.db.event.EventRepository
import rfm.com.data.db.post.PostRepository
import rfm.com.data.db.user.UserRepository
import rfm.com.routes.*
import rfm.com.security.hashing.HashingService
import rfm.com.security.token.TokenConfig
import rfm.com.security.token.TokenService
import rfm.com.services.EmailService
import io.ktor.server.application.*
import io.ktor.server.routing.*


fun Application.configureRouting(
    userRepository: UserRepository,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    eventRepository: EventRepository,
    postRepository: PostRepository,
    emailService: EmailService,
)  {
    routing {
        postRoutes( eventRepository, userRepository, postRepository )
        loginRoutes(hashingService, userRepository, tokenService, tokenConfig, emailService)
        eventRoutes(eventRepository, userRepository)
        dynamicJsProcessing()
        profileRoutes(userRepository)
        userRoutes(userRepository)
    }
}
