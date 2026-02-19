package rfm.com.auth.config

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import rfm.com.auth.model.Role
import rfm.com.auth.repository.RoleRepository

@Component
class DataInitializer(
    private val roleRepository: RoleRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val roles = listOf("USER", "ADMIN", "STAFF")
        
        roles.forEach { roleName ->
            if (roleRepository.findByName(roleName).isEmpty) {
                roleRepository.save(Role(name = roleName))
            }
        }
    }
}
