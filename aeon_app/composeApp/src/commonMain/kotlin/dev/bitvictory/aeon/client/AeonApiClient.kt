package dev.bitvictory.aeon.client

import dev.bitvictory.aeon.exceptions.AuthException
import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.aeonBody
import dev.bitvictory.aeon.model.api.system.SystemHealthDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationPatchDTO
import dev.bitvictory.aeon.service.IUserService
import dev.bitvictory.aeon.service.UserService
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
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

class AeonApiClient internal constructor(
	private val baseUrl: String,
	private val userService: IUserService,
	private val client: HttpClient
): AeonApi {

	constructor(
		baseUrl: String,
		userService: UserService,
	): this(baseUrl, userService, createClient(userService))

	companion object {
		@OptIn(ExperimentalSerializationApi::class)
		private fun createClient(userService: UserService) = HttpClient {
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

	override suspend fun getPrivacyInformation(): AeonResponse<PrivacyInformationDTO> {
		try {
			val response = client.get("$baseUrl/user/privacy/information")
			return response.aeonBody()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, AeonError(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

	override suspend fun patchPrivacyInformation(privacyInformationPatchDTO: PrivacyInformationPatchDTO): AeonResponse<Unit> {
		try {
			val response = client.patch("$baseUrl/user/privacy/information") {
				setBody(privacyInformationPatchDTO)
			}
			return response.aeonBody()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, AeonError(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

	override suspend fun getStatus(): AeonResponse<SystemHealthDTO> {
		try {
			val response = client.get("$baseUrl/system/health")
			return response.aeonBody()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, AeonError(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

}