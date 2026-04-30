package rfm.hillsongptapp.core.network.base

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import rfm.hillsongptapp.core.network.ktor.responses.ApiResponse
import rfm.hillsongptapp.core.network.result.NetworkException
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.core.network.result.toNetworkException

/**
 * Base API service providing common HTTP operations with proper error handling
 * Following modern Android architecture patterns
 */
abstract class BaseApiService(
    protected val httpClient: HttpClient,
    protected val baseUrl: String
) {
    
    /**
     * Safe GET request with automatic error handling
     */
    protected suspend inline fun <reified T> safeGet(
        endpoint: String,
        crossinline requestBuilder: HttpRequestBuilder.() -> Unit = {}
    ): NetworkResult<T> {
        return safeApiCall {
            httpClient.get("$baseUrl/$endpoint") {
                requestBuilder()
            }.bodyOrError()
        }
    }
    
    /**
     * Safe POST request with automatic error handling
     */
    protected suspend inline fun <reified T> safePost(
        endpoint: String,
        body: Any? = null,
        crossinline requestBuilder: HttpRequestBuilder.() -> Unit = {}
    ): NetworkResult<T> {
        return safeApiCall {
            httpClient.post("$baseUrl/$endpoint") {
                contentType(ContentType.Application.Json)
                body?.let { setBody(it) }
                requestBuilder()
            }.bodyOrError()
        }
    }
    
    /**
     * Safe PUT request with automatic error handling
     */
    protected suspend inline fun <reified T> safePut(
        endpoint: String,
        body: Any? = null,
        crossinline requestBuilder: HttpRequestBuilder.() -> Unit = {}
    ): NetworkResult<T> {
        return safeApiCall {
            httpClient.put("$baseUrl/$endpoint") {
                contentType(ContentType.Application.Json)
                body?.let { setBody(it) }
                requestBuilder()
            }.bodyOrError()
        }
    }
    
    /**
     * Safe PATCH request with automatic error handling
     */
    protected suspend inline fun <reified T> safePatch(
        endpoint: String,
        body: Any? = null,
        crossinline requestBuilder: HttpRequestBuilder.() -> Unit = {}
    ): NetworkResult<T> {
        return safeApiCall {
            httpClient.patch("$baseUrl/$endpoint") {
                contentType(ContentType.Application.Json)
                body?.let { setBody(it) }
                requestBuilder()
            }.bodyOrError()
        }
    }
    
    /**
     * Safe DELETE request with automatic error handling
     */
    protected suspend inline fun <reified T> safeDelete(
        endpoint: String,
        crossinline requestBuilder: HttpRequestBuilder.() -> Unit = {}
    ): NetworkResult<T> {
        return safeApiCall {
            httpClient.delete("$baseUrl/$endpoint") {
                requestBuilder()
            }.bodyOrError()
        }
    }

    protected suspend inline fun <reified T> HttpResponse.bodyOrError(): T {
        if (status.isSuccess()) {
            return body()
        }

        throw responseException()
    }

    protected suspend fun HttpResponse.responseException(): NetworkException {
        val errorMessage = try {
            body<ApiResponse<Any>>().message
        } catch (e: Exception) {
            status.description
        }

        return when (status) {
            io.ktor.http.HttpStatusCode.Unauthorized -> NetworkException.Unauthorized
            else -> NetworkException.HttpError(
                statusCode = status,
                errorMessage = errorMessage
            )
        }
    }
    
    /**
     * Generic safe API call wrapper
     */
    protected suspend inline fun <T> safeApiCall(
        crossinline apiCall: suspend () -> T
    ): NetworkResult<T> {
        return try {
            NetworkResult.Success(apiCall())
        } catch (e: Exception) {
            NetworkResult.Error(e.toNetworkException())
        }
    }
}
