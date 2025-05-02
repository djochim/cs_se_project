package dev.bitvictory.aeon.client.aeon

import dev.bitvictory.aeon.exceptions.AuthException
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.service.IUserService
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.serialization.protobuf.ProtoBuf

object AeonHttpClientFactory {

	fun create(userService: IUserService) = HttpClient {
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
			requestTimeoutMillis = 10_000 // 10 seconds
			connectTimeoutMillis = 5_000   // 5 seconds
			socketTimeoutMillis = 15_000   // 15 seconds
		}
		install(HttpRequestRetry) {
			retryOnServerErrors(maxRetries = 3)
			retryOnException(maxRetries = 3)
			exponentialDelay()
			modifyRequest { request ->
				request.headers.append("X-Retry-Count", retryCount.toString())
			}
		}
		install(Auth) {
			bearer {
				loadTokens {
					userService.userState.value.let {
						BearerTokens(it.accessToken, it.refreshToken)
					}
				}
				refreshTokens {
					when (val response = userService.refreshLogin()) {
						is AeonSuccessResponse ->
							BearerTokens(response.data.accessToken, response.data.refreshToken)

						is AeonErrorResponse   -> throw AuthException("Error refreshing token")
					}
				}
			}
		}
		defaultRequest {
			contentType(ContentType.Application.ProtoBuf)
		}
		followRedirects = true
		expectSuccess = false
	}

}