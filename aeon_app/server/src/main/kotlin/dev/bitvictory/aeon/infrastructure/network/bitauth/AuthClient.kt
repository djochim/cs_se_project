package dev.bitvictory.aeon.infrastructure.network.bitauth

import dev.bitvictory.aeon.configuration.UserContext
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategory
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntry
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntryName
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntryValue
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryKey
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryName
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataChangeRequest
import dev.bitvictory.aeon.core.domain.usecases.user.PersonaDataProvider
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.aeonBody
import dev.bitvictory.aeon.model.api.user.UpdateUserRequest
import dev.bitvictory.aeon.model.api.user.UserDTO
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

class AuthClient private constructor(): PersonaDataProvider {

	private lateinit var client: HttpClient
	private lateinit var baseUrl: String

	internal constructor(baseUrl: String, client: HttpClient): this() {
		this.client = client
		this.baseUrl = baseUrl
	}

	constructor(baseUrl: String): this() {
		this.baseUrl = baseUrl
		this.client = HttpClient {
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
	}

	suspend fun getUser(userContext: UserContext): AeonResponse<UserDTO> {
		val response = client.get("$baseUrl/v1/users") {
			headers.append(HttpHeaders.Authorization, userContext.tokenOrThrow())
		}
		return response.aeonBody<UserDTO>()
	}

	suspend fun patchUser(userContext: UserContext, updateUserRequest: UpdateUserRequest): AeonResponse<Unit> {
		val response = client.patch("$baseUrl/v1/users") {
			headers.append(HttpHeaders.Authorization, userContext.tokenOrThrow())
			setBody(updateUserRequest)
		}
		return response.aeonBody<Unit>()
	}

	override suspend fun getCategories(): List<PersonalDataCategoryKey> {
		return listOf(PersonalDataCategoryKey.PROFILE)
	}

	override suspend fun getPersonalData(userContext: UserContext): PersonalData {
		when (val user = getUser(userContext)) {
			is AeonSuccessResponse -> return mapUserToPersonalData(user.data)
			is AeonErrorResponse   -> throw Exception("Retrieving personal data failed with: $user")
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	override suspend fun patchPersonalData(userContext: UserContext, personalDataChangeRequest: PersonalDataChangeRequest) {
		var newName: PersonalDataCategoryEntryValue? = null
		var newEmail: PersonalDataCategoryEntryValue? = null
		personalDataChangeRequest.changes.forEach {
			when (it.name) {
				PersonalDataCategoryEntryName("name") -> newName = it.value
				PersonalDataCategoryEntryName("email") -> newEmail = it.value
			}
		}
		personalDataChangeRequest.deletions.forEach {
			when (it) {
				PersonalDataCategoryEntryName("name") -> newName = PersonalDataCategoryEntryValue("")
			}
		}
		if (newName == null && newEmail == null) return
		val patchRequest = UpdateUserRequest(name = newName?.s, email = newEmail?.s)
		when (val user = patchUser(userContext, patchRequest)) {
			is AeonSuccessResponse -> return
			is AeonErrorResponse   -> throw Exception("Updating personal data failed with: $user")
		}
	}

	private fun mapUserToPersonalData(user: UserDTO): PersonalData {
		val entries = listOf(
			PersonalDataCategoryEntry(PersonalDataCategoryEntryName("email"), PersonalDataCategoryEntryValue(user.email), false),
			PersonalDataCategoryEntry(PersonalDataCategoryEntryName("name"), PersonalDataCategoryEntryValue(user.name)),
		)
		return PersonalData(
			listOf(
				PersonalDataCategory(
					PersonalDataCategoryKey.PROFILE,
					PersonalDataCategoryName.PROFILE,
					entries.filter { it.value.s.isNotBlank() })
			)
		)
	}

}