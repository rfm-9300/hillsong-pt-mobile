package rfm.com.di
import rfm.com.data.db.event.EventRepository
import rfm.com.data.db.event.EventRepositoryImpl
import rfm.com.data.db.post.PostRepository
import rfm.com.data.db.post.PostRepositoryImpl
import rfm.com.data.db.user.UserRepository
import rfm.com.data.db.user.UserRepositoryImpl
import rfm.com.data.db.service.ServiceRepository
import rfm.com.data.db.service.ServiceRepositoryImpl
import rfm.com.data.db.kidsservice.KidsServiceRepository
import rfm.com.data.db.kidsservice.KidsServiceRepositoryImpl
import rfm.com.data.db.attendance.AttendanceRepository
import rfm.com.data.db.attendance.AttendanceRepositoryImpl
import rfm.com.security.token.TokenService
import rfm.com.security.token.JwtTokenService
import rfm.com.services.AttendanceService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import rfm.com.data.db.kid.KidRepository
import rfm.com.data.db.kid.KidRepositoryImpl

val appModule = module {
    singleOf(::EventRepositoryImpl) { bind<EventRepository>() }
    singleOf(::JwtTokenService) { bind<TokenService>() }
    singleOf(::PostRepositoryImpl) { bind<PostRepository>() }
    singleOf(::KidRepositoryImpl) { bind<KidRepository>() }
    singleOf(::ServiceRepositoryImpl) { bind<ServiceRepository>() }
    singleOf(::KidsServiceRepositoryImpl) { bind<KidsServiceRepository>() }
    singleOf(::AttendanceRepositoryImpl) { bind<AttendanceRepository>() }

    // Define UserRepositoryImpl with TokenService as a dependency
    single<UserRepository> {
        UserRepositoryImpl(tokenService = get())
    }
    
    // Define AttendanceService with its dependencies
    single {
        AttendanceService(
            attendanceRepository = get(),
            eventRepository = get(),
            serviceRepository = get(),
            kidsServiceRepository = get(),
            userRepository = get(),
            kidRepository = get()
        )
    }
}