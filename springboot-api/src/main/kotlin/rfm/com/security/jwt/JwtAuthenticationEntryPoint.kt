package rfm.com.security.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint::class.java)
    private val objectMapper = ObjectMapper()

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.error("Responding with unauthorized error. Message - {}", authException.message)
        
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        
        val errorResponse = mapOf(
            "success" to false,
            "message" to "Unauthorized: ${authException.message}",
            "timestamp" to LocalDateTime.now().toString(),
            "path" to request.requestURI
        )
        
        objectMapper.writeValue(response.outputStream, errorResponse)
    }
}