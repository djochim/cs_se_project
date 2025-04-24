package dev.bitvictory.aeon.screens.privacyinfo

import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationEntryDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationGroupDTO
import dev.bitvictory.aeon.service.IPrivacyService
import dev.bitvictory.aeon.service.IUserService
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.collections.shouldContain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PrivacyInformationViewModelTest {

	private val userService: IUserService = mock(MockMode.autoUnit)
	private val privacyService: IPrivacyService = mock(MockMode.autoUnit)

	@OptIn(ExperimentalCoroutinesApi::class)
	@BeforeTest
	fun setup() {
		Dispatchers.setMain(UnconfinedTestDispatcher())
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@AfterTest
	fun tearDown() {
		Dispatchers.resetMain()
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `state changes after load finished`() = runTest {
		val dto = PrivacyInformationDTO(
			listOf(
				PrivacyInformationGroupDTO(
					"Key", "test", listOf(
						PrivacyInformationEntryDTO("key", "value")
					)
				)
			)
		)
		everySuspend { privacyService.getPrivacyInformation() } returns AeonSuccessResponse(dto)

		val privacyInformationViewModel = PrivacyInformationViewModel(privacyService, userService)
		val states = mutableListOf<PrivacyInformationUIState>()
		val job = launch {
			privacyInformationViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()

		job.cancel()

		states shouldContain PrivacyInformationUIState(privacyInformation = dto)
	}

	@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
	@Test
	fun `state changes after load failed`() = runTest {
		val id = Uuid.random().toHexString()
		everySuspend { privacyService.getPrivacyInformation() } returns AeonErrorResponse(
			500,
			AeonError(correlationId = id, message = "failure"),
			ErrorType.CLIENT_ERROR
		)

		val privacyInformationViewModel = PrivacyInformationViewModel(privacyService, userService)
		val states = mutableListOf<PrivacyInformationUIState>()
		val job = launch {
			privacyInformationViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()

		job.cancel()

		states shouldContain PrivacyInformationUIState(error = AeonError(correlationId = id, message = "failure"))
	}

}