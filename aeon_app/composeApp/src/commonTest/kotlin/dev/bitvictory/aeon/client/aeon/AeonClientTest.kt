package dev.bitvictory.aeon.client.aeon

import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationEntryDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationGroupDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationKeyDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationPatchDTO
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.test.Test

class AeonClientTest {

	companion object {
		const val BASE_URL = "http://localhost:8080"
	}

	@OptIn(ExperimentalSerializationApi::class)
	private fun getAeonClient(mockEngine: MockEngine): AeonApiClient {
		val client = HttpClient(mockEngine) {
			install(ContentNegotiation) {
				protobuf(protobuf = ProtoBuf {
					encodeDefaults = true
				})
			}

			defaultRequest {
				contentType(ContentType.Application.ProtoBuf)
			}
		}

		return AeonApiClient(BASE_URL, client)
	}

	@OptIn(ExperimentalSerializationApi::class)
	@Test
	fun `get personal data of user`() = runTest {
		val returnedDTO = PrivacyInformationDTO(listOf(PrivacyInformationGroupDTO("key", "name", listOf(PrivacyInformationEntryDTO("key", "value")))))
		val jsonResponse = ProtoBuf.encodeToByteArray(returnedDTO)

		val mockEngine = MockEngine { _ ->
			respond(
				content = jsonResponse,
				status = HttpStatusCode.OK,
				headers = headersOf("Content-Type", ContentType.Application.ProtoBuf.toString())
			)
		}
		val aeonClient = getAeonClient(mockEngine)

		val result = aeonClient.getPrivacyInformation()

		result.shouldBeInstanceOf<AeonSuccessResponse<PrivacyInformationDTO>>()
		result.data shouldBe returnedDTO
	}

	@OptIn(ExperimentalSerializationApi::class)
	@Test
	fun `get personal data with empty result`() = runTest {
		val returnedDTO = PrivacyInformationDTO(emptyList())
		val jsonResponse = ProtoBuf.encodeToByteArray(returnedDTO)

		val mockEngine = MockEngine { _ ->
			respond(
				content = jsonResponse,
				status = HttpStatusCode.OK,
				headers = headersOf("Content-Type", ContentType.Application.ProtoBuf.toString())
			)
		}
		val aeonApiClient = getAeonClient(mockEngine)

		val result = aeonApiClient.getPrivacyInformation()

		result.shouldBeInstanceOf<AeonSuccessResponse<PrivacyInformationDTO>>()
		result.data shouldBe returnedDTO
	}

	@OptIn(ExperimentalSerializationApi::class)
	@Test
	fun `get personal data fails`() = runTest {
		val errorResponse = AeonError(
			message = "The request has a bad format",
			details = mapOf("email" to "Must be a valid")
		)
		val mockEngine = MockEngine { _ ->
			respond(
				content = ProtoBuf.encodeToByteArray(errorResponse),
				status = HttpStatusCode.BadRequest,
				headers = headersOf("Content-Type", ContentType.Application.ProtoBuf.toString())
			)
		}
		val aeonClient = getAeonClient(mockEngine)

		val result = aeonClient.getPrivacyInformation()

		result.shouldBeInstanceOf<AeonErrorResponse<PrivacyInformationDTO>>()
		result.error.message shouldBe "The request has a bad format"
		result.error.details shouldBe mapOf("email" to "Must be a valid")
		result.statusCode shouldBe 400
	}

	@Test
	fun `get personal data connection error`() = runTest {
		val mockEngine = MockEngine { _ ->
			throw IOException("Connection error")
		}
		val aeonClient = getAeonClient(mockEngine)

		aeonClient.getPrivacyInformation().shouldBeInstanceOf<AeonErrorResponse<PrivacyInformationDTO>>()
	}

	@OptIn(ExperimentalSerializationApi::class)
	@Test
	fun `patch personal data`() = runTest {
		var receivedBodyBytes: ByteArray? = null

		val mockEngine = MockEngine { request ->
			receivedBodyBytes = (request.body as ByteArrayContent).bytes()
			respond(
				content = "",
				status = HttpStatusCode.OK
			)
		}
		val aeonClient = getAeonClient(mockEngine)

		val patchDTO = PrivacyInformationPatchDTO(
			"key",
			listOf(PrivacyInformationKeyDTO("key1"), PrivacyInformationKeyDTO("key2")),
			listOf(PrivacyInformationEntryDTO("key3", "value3"))
		)
		aeonClient.patchPrivacyInformation(patchDTO).shouldBeInstanceOf<AeonSuccessResponse<Unit>>()

		receivedBodyBytes shouldNot beNull()
		ProtoBuf.decodeFromByteArray<PrivacyInformationPatchDTO>(receivedBodyBytes!!) shouldBe patchDTO
	}

	@OptIn(ExperimentalSerializationApi::class)
	@Test
	fun `patch personal data fails`() = runTest {
		var receivedBodyBytes: ByteArray? = null

		val mockEngine = MockEngine { request ->
			receivedBodyBytes = (request.body as ByteArrayContent).bytes()
			respond(
				content = "",
				status = HttpStatusCode.BadRequest
			)
		}
		val aeonClient = getAeonClient(mockEngine)

		val patchDTO = PrivacyInformationPatchDTO(
			"key",
			listOf(PrivacyInformationKeyDTO("key1"), PrivacyInformationKeyDTO("key2")),
			listOf(PrivacyInformationEntryDTO("key3", "value3"))
		)
		aeonClient.patchPrivacyInformation(patchDTO).shouldBeInstanceOf<AeonErrorResponse<Unit>>()

		receivedBodyBytes shouldNot beNull()
		ProtoBuf.decodeFromByteArray<PrivacyInformationPatchDTO>(receivedBodyBytes!!) shouldBe patchDTO
	}

	@Test
	fun `patch personal data connection error`() = runTest {
		val mockEngine = MockEngine { _ ->
			throw IOException("Connection error")
		}
		val aeonClient = getAeonClient(mockEngine)

		val patchDTO = PrivacyInformationPatchDTO(
			"key",
			listOf(PrivacyInformationKeyDTO("key1"), PrivacyInformationKeyDTO("key2")),
			listOf(PrivacyInformationEntryDTO("key3", "value3"))
		)
		aeonClient.patchPrivacyInformation(patchDTO).shouldBeInstanceOf<AeonErrorResponse<Unit>>()
	}
}