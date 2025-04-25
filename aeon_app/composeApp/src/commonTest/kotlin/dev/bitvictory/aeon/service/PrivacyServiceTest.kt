package dev.bitvictory.aeon.service

import dev.bitvictory.aeon.client.aeon.AeonApi
import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationEntryDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationGroupDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationKeyDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationPatchDTO
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class PrivacyServiceTest {
	val aeonApi: AeonApi = mock(MockMode.autoUnit)
	val privacyService = PrivacyService(aeonApi)

	@Test
	fun getPrivacyInformation_empty() {
		runBlocking {
			everySuspend { aeonApi.getPrivacyInformation() } returns AeonSuccessResponse(PrivacyInformationDTO(emptyList()))

			val result = privacyService.getPrivacyInformation()

			result.shouldBeInstanceOf<AeonSuccessResponse<PrivacyInformationDTO>>()
			result.data shouldBe PrivacyInformationDTO(emptyList())
		}
	}

	@Test
	fun getPrivacyInformation() {
		runBlocking {
			val info = PrivacyInformationDTO(listOf(PrivacyInformationGroupDTO("Key", "test", listOf(PrivacyInformationEntryDTO("key", "value")))))
			everySuspend { aeonApi.getPrivacyInformation() } returns AeonSuccessResponse(info)
			val result = privacyService.getPrivacyInformation()

			result.shouldBeInstanceOf<AeonSuccessResponse<PrivacyInformationDTO>>()
			result.data shouldBe info
		}
	}

	@Test
	fun getPrivacyInformation_failed() {
		runBlocking {
			everySuspend { aeonApi.getPrivacyInformation() } returns AeonErrorResponse(500, AeonError(message = "error"), ErrorType.CLIENT_ERROR)

			val result = privacyService.getPrivacyInformation()

			result.shouldBeInstanceOf<AeonErrorResponse<PrivacyInformationDTO>>()
			result.error.shouldBeInstanceOf<AeonError>()
			result.error.message shouldBe "error"
		}

	}

	@Test
	fun deletePrivacyInformation_success() = runTest {
		everySuspend { aeonApi.patchPrivacyInformation(any()) } returns AeonSuccessResponse(Unit)

		val result = privacyService.deletePrivacyInformation("Key", "key")

		result.shouldBeInstanceOf<AeonSuccessResponse<Unit>>()

		verifySuspend { aeonApi.patchPrivacyInformation(PrivacyInformationPatchDTO("Key", listOf(PrivacyInformationKeyDTO("key")), emptyList())) }
	}

	@Test
	fun deletePrivacyInformation_fails() = runTest {
		everySuspend { aeonApi.patchPrivacyInformation(any()) } returns AeonErrorResponse(
			200,
			AeonError(message = ""),
			ErrorType.CLIENT_ERROR
		)

		val result = privacyService.deletePrivacyInformation("Key", "key")

		result.shouldBeInstanceOf<AeonErrorResponse<Unit>>()

		verifySuspend { aeonApi.patchPrivacyInformation(PrivacyInformationPatchDTO("Key", listOf(PrivacyInformationKeyDTO("key")), emptyList())) }
	}
}