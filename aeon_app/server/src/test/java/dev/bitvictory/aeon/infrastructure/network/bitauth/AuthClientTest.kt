package dev.bitvictory.aeon.infrastructure.network.bitauth

import dev.bitvictory.aeon.configuration.UserContext
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategory
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntry
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntryName
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntryValue
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryName
import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.api.user.UserDTO
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class AuthClientTest {

	companion object {
		const val BASE_URL = "http://localhost:8080"
	}

	private fun getAuthClient(mockEngine: MockEngine): AuthClient {
		val client = HttpClient(mockEngine) {
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
		}

		return AuthClient(BASE_URL, client)
	}

	@Test
	fun `get user returns user`(@MockK userContext: UserContext) = runTest {
		val expectedToken = "my-very-secret-jwt-token"
		val jsonResponse = """
			{
			  "type": "dev.bitvictory.model.user.RegisteredUser",
			  "_id": "6fdsajnhasdf4576cda",
			  "accountType": "USER",
			  "accountStatus": "ACTIVE",
			  "sendAnalyticalData": false,
			  "acceptedPrivacyVersion": 0,
			  "twoFactorToken": "",
			  "email": "test@jochim.dev",
			  "name": "tester",
			  "profileImage": null,
			  "password": "[MASKED]"
			}
		""".trimIndent()
		var receivedToken: String? = null

		val mockEngine = MockEngine { request ->
			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
			respond(
				content = jsonResponse,
				status = HttpStatusCode.OK,
				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
			)
		}
		val authClient = getAuthClient(mockEngine)

		every { userContext.tokenOrThrow() } returns expectedToken

		val result = authClient.getUser(userContext)

		receivedToken shouldBe expectedToken
		result.shouldBeInstanceOf<AeonSuccessResponse<UserDTO>>()
		result.data shouldBe UserDTO("test@jochim.dev", "tester", "USER", false, 0)
	}

	@Test
	fun `get user is failing and returns failed response`(@MockK userContext: UserContext) = runTest {
		val expectedToken = "my-very-secret-jwt-token"
		val jsonResponse = """
			{
			  "correlationId": "e2a52070-b79b-496b-87d3-6e294a270272",
			  "message": "The request has a bad format",
			  "details": {
				"email": "Must be a valid"
			  }
			}
		""".trimIndent()
		var receivedToken: String? = null

		val mockEngine = MockEngine { request ->
			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
			respond(
				content = jsonResponse,
				status = HttpStatusCode.BadRequest,
				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
			)
		}
		val authClient = getAuthClient(mockEngine)

		every { userContext.tokenOrThrow() } returns expectedToken

		val result = authClient.getUser(userContext)

		receivedToken shouldBe expectedToken
		result.shouldBeInstanceOf<AeonErrorResponse<UserDTO>>()
		result.statusCode shouldBe 400
		result.type shouldBe ErrorType.CLIENT_ERROR
		result.error shouldBe AeonError("e2a52070-b79b-496b-87d3-6e294a270272", "The request has a bad format", mapOf("email" to "Must be a valid"))
	}

	@Test
	fun `get personal data of user`(@MockK userContext: UserContext) = runTest {
		val expectedToken = "my-very-secret-jwt-token"
		val jsonResponse = """
			{
			  "type": "dev.bitvictory.model.user.RegisteredUser",
			  "_id": "6fdsajnhasdf4576cda",
			  "accountType": "USER",
			  "accountStatus": "ACTIVE",
			  "sendAnalyticalData": false,
			  "acceptedPrivacyVersion": 0,
			  "twoFactorToken": "",
			  "email": "test@jochim.dev",
			  "name": "tester",
			  "profileImage": null,
			  "password": "[MASKED]"
			}
		""".trimIndent()
		var receivedToken: String? = null

		val mockEngine = MockEngine { request ->
			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
			respond(
				content = jsonResponse,
				status = HttpStatusCode.OK,
				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
			)
		}
		val authClient = getAuthClient(mockEngine)

		every { userContext.tokenOrThrow() } returns expectedToken

		val result = authClient.getPersonalData(userContext)

		receivedToken shouldBe expectedToken
		result shouldBe PersonalData(
			listOf(
				PersonalDataCategory(
					PersonalDataCategoryName("Profile Data"), listOf(
						PersonalDataCategoryEntry(PersonalDataCategoryEntryName("email"), PersonalDataCategoryEntryValue("test@jochim.dev")),
						PersonalDataCategoryEntry(PersonalDataCategoryEntryName("name"), PersonalDataCategoryEntryValue("tester")),
					)
				)
			)
		)
	}

	@Test
	fun `get personal data filters empty name`(@MockK userContext: UserContext) = runTest {
		val expectedToken = "my-very-secret-jwt-token"
		val jsonResponse = """
			{
			  "type": "dev.bitvictory.model.user.RegisteredUser",
			  "_id": "6fdsajnhasdf4576cda",
			  "accountType": "USER",
			  "accountStatus": "ACTIVE",
			  "sendAnalyticalData": false,
			  "acceptedPrivacyVersion": 0,
			  "twoFactorToken": "",
			  "email": "test@jochim.dev",
			  "name": "",
			  "profileImage": null,
			  "password": "[MASKED]"
			}
		""".trimIndent()
		var receivedToken: String? = null

		val mockEngine = MockEngine { request ->
			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
			respond(
				content = jsonResponse,
				status = HttpStatusCode.OK,
				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
			)
		}
		val authClient = getAuthClient(mockEngine)

		every { userContext.tokenOrThrow() } returns expectedToken

		val result = authClient.getPersonalData(userContext)

		receivedToken shouldBe expectedToken
		result shouldBe PersonalData(
			listOf(
				PersonalDataCategory(
					PersonalDataCategoryName("Profile Data"), listOf(
						PersonalDataCategoryEntry(PersonalDataCategoryEntryName("email"), PersonalDataCategoryEntryValue("test@jochim.dev")),
					)
				)
			)
		)
	}

	@Test
	fun `get personal data filters empty email`(@MockK userContext: UserContext) = runTest {
		val expectedToken = "my-very-secret-jwt-token"
		val jsonResponse = """
			{
			  "type": "dev.bitvictory.model.user.RegisteredUser",
			  "_id": "6fdsajnhasdf4576cda",
			  "accountType": "USER",
			  "accountStatus": "ACTIVE",
			  "sendAnalyticalData": false,
			  "acceptedPrivacyVersion": 0,
			  "twoFactorToken": "",
			  "email": "",
			  "name": "tester",
			  "profileImage": null,
			  "password": "[MASKED]"
			}
		""".trimIndent()
		var receivedToken: String? = null

		val mockEngine = MockEngine { request ->
			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
			respond(
				content = jsonResponse,
				status = HttpStatusCode.OK,
				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
			)
		}
		val authClient = getAuthClient(mockEngine)

		every { userContext.tokenOrThrow() } returns expectedToken

		val result = authClient.getPersonalData(userContext)

		receivedToken shouldBe expectedToken
		result shouldBe PersonalData(
			listOf(
				PersonalDataCategory(
					PersonalDataCategoryName("Profile Data"), listOf(
						PersonalDataCategoryEntry(PersonalDataCategoryEntryName("name"), PersonalDataCategoryEntryValue("tester")),
					)
				)
			)
		)
	}

	@Test
	fun `get user is failing and get personal data throws exception`(@MockK userContext: UserContext) = runTest {
		val expectedToken = "my-very-secret-jwt-token"
		val jsonResponse = """
			{
			  "correlationId": "e2a52070-b79b-496b-87d3-6e294a270272",
			  "message": "The request has a bad format",
			  "details": {
				"email": "Must be a valid"
			  }
			}
		""".trimIndent()
		var receivedToken: String? = null

		val mockEngine = MockEngine { request ->
			receivedToken = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
			respond(
				content = jsonResponse,
				status = HttpStatusCode.BadRequest,
				headers = headersOf("Content-Type", ContentType.Application.Json.toString())
			)
		}
		val authClient = getAuthClient(mockEngine)

		every { userContext.tokenOrThrow() } returns expectedToken

		assertThrows<Exception> { authClient.getPersonalData(userContext) }

		receivedToken shouldBe expectedToken
	}

}