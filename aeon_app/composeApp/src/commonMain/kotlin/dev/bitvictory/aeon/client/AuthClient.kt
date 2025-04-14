package dev.bitvictory.aeon.client

import dev.bitvictory.aeon.exceptions.AuthException
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.Error
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.api.user.UpdateUserRequest
import dev.bitvictory.aeon.model.api.user.UserDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginRefreshDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import dev.bitvictory.aeon.storage.SharedSettingsHelper
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
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class AuthClient(
	private val baseUrl: String,
	private val sharedSettingsHelper: SharedSettingsHelper
): IAMApi {

	private val _tokenRefreshEvents = MutableSharedFlow<AeonResponse<TokenDTO>>()
	override val tokenRefreshEvents: SharedFlow<AeonResponse<TokenDTO>> = _tokenRefreshEvents

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
		install(Auth) {
			bearer {
				loadTokens {
					sharedSettingsHelper.token.let {
						val token = it
						if (token == null) null else
							BearerTokens(token.accessToken, token.refreshToken)
					}
				}
				refreshTokens {
					val token = sharedSettingsHelper.token?.refreshToken
					if (token == null) {
						null
					} else {
						when (val response = refreshLogin(LoginRefreshDTO(token))) {
							is AeonSuccessResponse ->
								BearerTokens(response.data.accessToken, response.data.refreshToken).also { _tokenRefreshEvents.emit(response) }

							is AeonErrorResponse   -> throw AuthException("Error refreshing token")
						}
					}
				}
			}
		}
		defaultRequest {
			contentType(ContentType.Application.Json)
		}
		followRedirects = true
		expectSuccess = false
	}

	override suspend fun login(login: LoginDTO): AeonResponse<TokenDTO> {
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

	override suspend fun refreshLogin(refresh: LoginRefreshDTO): AeonResponse<TokenDTO> {
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

	override suspend fun getUser(): AeonResponse<UserDTO> {
		try {
			val response = client.get("$baseUrl/users")
			return response.aeonBody<UserDTO>()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, Error(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

	override suspend fun updateUser(user: UpdateUserRequest): AeonResponse<Any> {
		try {
			val response = client.patch("$baseUrl/users") {
				contentType(ContentType.Application.Json)
				setBody(user)
			}
			return response.aeonBody<Any>()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, Error(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

}