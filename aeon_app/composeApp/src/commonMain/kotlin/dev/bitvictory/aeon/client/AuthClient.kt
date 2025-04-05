package dev.bitvictory.aeon.client

import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.Error
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.api.user.auth.LoginDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginRefreshDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

class AuthClient(
	private val baseUrl: String
) {

	private val client = HttpClient {
		install(Logging) {
			level = LogLevel.ALL
			sanitizeHeader { header -> header == HttpHeaders.Authorization }
		}
		install(ContentNegotiation) {
			json(json = Json {
				encodeDefaults = true
				isLenient = true
				allowSpecialFloatingPointValues = true
				allowStructuredMapKeys = true
				prettyPrint = false
				useArrayPolymorphism = false
				ignoreUnknownKeys = true
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
			contentType(ContentType.Application.Json)
		}
		followRedirects = true
		expectSuccess = false
	}
	
	suspend fun login(login: LoginDTO): AeonResponse<TokenDTO> {
		try {
			val response = client.post("$baseUrl/login") {
				contentType(ContentType.Application.Json)
				setBody(login)
			}
			return response.aeonBody<TokenDTO>()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, Error(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

	suspend fun refreshLogin(refresh: LoginRefreshDTO): AeonResponse<TokenDTO> {
		try {
			val response = client.post("$baseUrl/login/refresh") {
				contentType(ContentType.Application.Json)
				setBody(refresh)
			}
			return response.aeonBody<TokenDTO>()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, Error(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

}