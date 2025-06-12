package rfm.hillsongptapp.core.data.repository.ktor


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.InternalAPI
import rfm.hillsongptapp.core.data.repository.ktor.requests.LoginRequest
import rfm.hillsongptapp.core.data.repository.ktor.responses.LoginResponse

class ApiService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
) {

    suspend fun login (request: LoginRequest): LoginResponse {
        return httpClient.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }





    private suspend inline fun <reified T> fetchData(endpoint: String): T =
        httpClient.get("$baseUrl/$endpoint").body()
}
