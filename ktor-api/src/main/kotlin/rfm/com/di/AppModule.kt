package rfm.com.di
import rfm.com.data.db.event.EventRepository
import rfm.com.data.db.event.EventRepositoryImpl
import rfm.com.data.db.post.PostRepository
import rfm.com.data.db.post.PostRepositoryImpl
import rfm.com.data.db.user.UserRepository
import rfm.com.data.db.user.UserRepositoryImpl
import rfm.com.security.token.TokenService
import rfm.com.security.token.JwtTokenService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::EventRepositoryImpl) { bind<EventRepository>() }
    singleOf(::JwtTokenService) { bind<TokenService>() }
    singleOf(::PostRepositoryImpl) { bind<PostRepository>() }

    // Define UserRepositoryImpl with TokenService as a dependency
    single<UserRepository> {
        UserRepositoryImpl(tokenService = get())
    }

}