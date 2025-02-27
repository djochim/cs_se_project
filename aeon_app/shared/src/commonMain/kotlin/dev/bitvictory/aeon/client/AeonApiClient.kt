package dev.bitvictory.aeon.client

import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.api.system.SystemHealthDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

class AeonApiClient(
	private val baseUrl: String,
) {

	companion object {
		const val ADVISORY_ENDPOINT = "advisories"
	}

	@OptIn(ExperimentalSerializationApi::class)
	private val client = HttpClient {
		install(Logging) {
			level = LogLevel.ALL
			sanitizeHeader { header -> header == HttpHeaders.Authorization }
		}
		install(ContentNegotiation) {
			protobuf(protobuf = ProtoBuf {
				encodeDefaults = true
			})
		}
		install(HttpTimeout) {
			requestTimeoutMillis = 10_000
			connectTimeoutMillis = 1_000
		}
		install(HttpRequestRetry) {
			retryOnServerErrors(maxRetries = 1)
			exponentialDelay()
			modifyRequest { request ->
				request.headers.append("X-Retry-Count", retryCount.toString())
			}
		}
		defaultRequest {
			contentType(ContentType.Application.ProtoBuf)
		}
		followRedirects = true
		expectSuccess = false
	}

	suspend fun getStatus(): AeonResponse<SystemHealthDTO> {
		try {
			val response = client.get("$baseUrl/system/health")
			return response.aeonBody()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, "Error connecting to the server", ErrorType.UNAVAILABLE_SERVER)
		}
	}

}

suspend inline fun <reified T> HttpResponse.aeonBody(): AeonResponse<T> {
	if (this.status.isSuccess()) {
		return AeonSuccessResponse(this.body())
	}
	if (this.status.value in 500..599) {
		return AeonErrorResponse(this.status.value, this.status.description, ErrorType.SERVER_ERROR)
	}
	if (this.status.value in 400..499) {
		return AeonErrorResponse(this.status.value, this.status.description, ErrorType.CLIENT_ERROR)
	}
	return AeonErrorResponse(this.status.value, this.status.description, ErrorType.UNKNOWN_ERROR)
}