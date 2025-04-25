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
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
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

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `state changes when deleting successfully`() = runTest {
		val dto = PrivacyInformationDTO(
			listOf(
				PrivacyInformationGroupDTO(
					"Key", "test", listOf(
						PrivacyInformationEntryDTO("key", "value")
					)
				),
				PrivacyInformationGroupDTO(
					"Key2", "test", listOf(
						PrivacyInformationEntryDTO("key", "value"),
						PrivacyInformationEntryDTO("key2", "value")
					)
				)
			)
		)

		everySuspend { privacyService.getPrivacyInformation() } returns AeonSuccessResponse(dto)
		everySuspend { privacyService.deletePrivacyInformation(any(), any()) } returns AeonSuccessResponse(Unit)

		val privacyInformationViewModel = PrivacyInformationViewModel(privacyService, userService)
		val states = mutableListOf<PrivacyInformationUIState>()
		val snacks = mutableListOf<String>()
		val job = launch {
			privacyInformationViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
			privacyInformationViewModel.snackbarEvent.collect { snackbar ->
				snacks.add(snackbar)
			}
		}
		advanceUntilIdle()
		privacyInformationViewModel.deletePrivacyInformation("Key2", "key")
		advanceUntilIdle()

		job.cancel()

		val dtoAfterDeletion = PrivacyInformationDTO(
			listOf(
				PrivacyInformationGroupDTO(
					"Key", "test", listOf(
						PrivacyInformationEntryDTO("key", "value")
					)
				),
				PrivacyInformationGroupDTO(
					"Key2", "test", listOf(
						PrivacyInformationEntryDTO("key2", "value")
					)
				)
			)
		)
		states shouldContain PrivacyInformationUIState(privacyInformation = dto)
		states shouldContain PrivacyInformationUIState(privacyInformation = dtoAfterDeletion)
		privacyInformationViewModel.uiState.value.privacyInformation shouldBe dtoAfterDeletion

		snacks shouldBe emptyList()
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `state changes when deleting fails`() = runTest {
		val dto = PrivacyInformationDTO(
			listOf(
				PrivacyInformationGroupDTO(
					"Key", "test", listOf(
						PrivacyInformationEntryDTO("key", "value")
					)
				),
				PrivacyInformationGroupDTO(
					"Key2", "test", listOf(
						PrivacyInformationEntryDTO("key", "value"),
						PrivacyInformationEntryDTO("key2", "value")
					)
				)
			)
		)

		everySuspend { privacyService.getPrivacyInformation() } returns AeonSuccessResponse(dto)
		everySuspend { privacyService.deletePrivacyInformation(any(), any()) } returns AeonErrorResponse(
			200,
			AeonError(message = ""),
			ErrorType.CLIENT_ERROR
		)

		val privacyInformationViewModel = PrivacyInformationViewModel(privacyService, userService)
		val states = mutableListOf<PrivacyInformationUIState>()
		val snacks = mutableListOf<String>()
		val job = launch {
			privacyInformationViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		val jobSnack = launch {
			privacyInformationViewModel.snackbarEvent.collect { snackbar ->
				snacks.add(snackbar)
			}
		}
		advanceUntilIdle()
		privacyInformationViewModel.deletePrivacyInformation("Key2", "key")
		advanceUntilIdle()

		job.cancel()
		jobSnack.cancel()

		val dtoAfterDeletion = PrivacyInformationDTO(
			listOf(
				PrivacyInformationGroupDTO(
					"Key", "test", listOf(
						PrivacyInformationEntryDTO("key", "value")
					)
				),
				PrivacyInformationGroupDTO(
					"Key2", "test", listOf(
						PrivacyInformationEntryDTO("key", "value"),
						PrivacyInformationEntryDTO("key2", "value")
					)
				)
			)
		)
		states shouldContain PrivacyInformationUIState(privacyInformation = dto)
		privacyInformationViewModel.uiState.value.privacyInformation shouldBe dtoAfterDeletion

		snacks shouldBe listOf("Failed to delete privacy information entry")
	}

}