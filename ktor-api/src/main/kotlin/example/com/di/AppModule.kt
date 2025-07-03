package example.com.di
import example.com.data.db.event.EventRepository
import example.com.data.db.event.EventRepositoryImpl
import example.com.data.db.post.PostRepository
import example.com.data.db.post.PostRepositoryImpl
import example.com.data.db.user.UserRepository
import example.com.data.db.user.UserRepositoryImpl
import example.com.data.utils.SseManager
import example.com.security.token.TokenService
import example.com.security.token.JwtTokenService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    single { SseManager() }
    singleOf(::EventRepositoryImpl) { bind<EventRepository>() }
    singleOf(::JwtTokenService) { bind<TokenService>() }
    singleOf(::PostRepositoryImpl) { bind<PostRepository>() }

    // Define UserRepositoryImpl with TokenService as a dependency
    single<UserRepository> {
        UserRepositoryImpl(tokenService = get())
    }

}