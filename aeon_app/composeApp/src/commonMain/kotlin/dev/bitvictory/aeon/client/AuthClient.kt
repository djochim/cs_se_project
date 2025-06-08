package dev.bitvictory.aeon.client

import dev.bitvictory.aeon.exceptions.AuthException
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.api.user.UpdateUserRequest
import dev.bitvictory.aeon.model.api.user.UserDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginRefreshDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import dev.bitvictory.aeon.model.common.util.requestWrapper
import dev.bitvictory.aeon.storage.LocalKeyValueStore
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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class AuthClient(
	private val baseUrl: String,
	private val localKeyValueStore: LocalKeyValueStore
): IAMApi {

	private val _tokenRefreshEvents = MutableSharedFlow<AeonResponse<TokenDTO>>()
	override val tokenRefreshEvents: SharedFlow<AeonResponse<TokenDTO>> = _tokenRefreshEvents

	/**
	 * The HTTP client used for making API requests.
	 *
	 * It is configured with the following features:
	 * - Logging: Logs all requests and responses, sanitizing the Authorization header.
	 * - ContentNegotiation: Uses JSON for serialization and deserialization, with lenient parsing and ignoring unknown keys.
	 * - HttpTimeout: Sets a request timeout of 10 seconds and a connect timeout of 1 second.
	 * - HttpRequestRetry: Retries requests once on server errors with exponential backoff, adding an "X-Retry-Count" header.
	 * - Auth: Handles Bearer token authentication, loading tokens from [localKeyValueStore] and refreshing them when necessary.
	 * - DefaultRequest: Sets the default content type to "application/json".
	 * - FollowRedirects: Allows following redirects.
	 * - ExpectSuccess: Does not throw an exception on non-2xx responses.
	 */
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
					localKeyValueStore.token.let {
						val token = it
						if (token == null) null else
							BearerTokens(token.accessToken, token.refreshToken)
					}
				}
				refreshTokens {
					val token = localKeyValueStore.token?.refreshToken
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

	override suspend fun login(login: LoginDTO): AeonResponse<TokenDTO> = requestWrapper {
		client.post("$baseUrl/login") {
			contentType(ContentType.Application.Json)
			setBody(login)
		}
	}

	override suspend fun refreshLogin(refresh: LoginRefreshDTO): AeonResponse<TokenDTO> = requestWrapper {
		client.post("$baseUrl/login/refresh") {
			contentType(ContentType.Application.Json)
			setBody(refresh)
		}
	}

	override suspend fun getUser(): AeonResponse<UserDTO> = requestWrapper {
		client.get("$baseUrl/users")
	}

	override suspend fun updateUser(user: UpdateUserRequest): AeonResponse<Any> = requestWrapper {
		client.patch("$baseUrl/users") {
			contentType(ContentType.Application.Json)
			setBody(user)
		}
	}

}