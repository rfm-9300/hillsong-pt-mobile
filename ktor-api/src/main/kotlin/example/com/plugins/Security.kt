package example.com.plugins

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import example.com.security.token.TokenConfig
import io.ktor.server.application.*

fun Application.configureSecurity(config: TokenConfig) {
    authentication {
        jwt {
            val environment = this@configureSecurity.environment
            realm = this@configureSecurity.environment.config.property("jwt.realm").getString()
            verifier(
                JWT
                    .require(Algorithm.HMAC256(config.secret))
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .build()
            )
            validate { jwtCredential ->
                if(jwtCredential.payload.audience.contains(config.audience)) {
                    JWTPrincipal(jwtCredential.payload)
                } else null
            }
        }
    }
}