package dev.bitvictory.aeon.screens.chat

import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.api.AuthorDTO
import dev.bitvictory.aeon.model.api.advisory.AdvisoryDTO
import dev.bitvictory.aeon.model.api.advisory.AdvisoryIdDTO
import dev.bitvictory.aeon.model.api.advisory.MessageDTO
import dev.bitvictory.aeon.model.api.advisory.StringMessageDTO
import dev.bitvictory.aeon.model.api.advisory.request.AdvisoryMessageRequest
import dev.bitvictory.aeon.service.IAdvisorService
import dev.bitvictory.aeon.service.IUserService
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class ChatViewModelTest {

	private val userService: IUserService = mock(MockMode.autoUnit)
	private val advisorService: IAdvisorService = mock(MockMode.autoUnit)

	private lateinit var chatViewModel: ChatViewModel

	@OptIn(ExperimentalCoroutinesApi::class)
	@BeforeTest
	fun setup() {
		Dispatchers.setMain(UnconfinedTestDispatcher())
		chatViewModel = ChatViewModel(advisorService, userService)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@AfterTest
	fun tearDown() {
		Dispatchers.resetMain()
	}

	@Test
	fun `getUiState initial state`() {
		chatViewModel.uiState.value shouldBe ChatUIState()
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `new message text state changes`() = runTest {
		val states = mutableListOf<ChatUIState>()
		val job = launch {
			chatViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()

		chatViewModel.changeNewMessage("first")
		advanceUntilIdle()
		chatViewModel.changeNewMessage("first.second")
		advanceUntilIdle()
		job.cancel()

		states shouldContainInOrder listOf(
			ChatUIState(),
			ChatUIState(newMessage = "first"),
			ChatUIState(newMessage = "first.second")
		)
		chatViewModel.uiState.value shouldBe ChatUIState(newMessage = "first.second")
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `new advisory state changes`() = runTest {
		val message = "message"
		val now = Clock.System.now()

		everySuspend { advisorService.initiateAdvisory(any()) } returns AeonSuccessResponse(
			AdvisoryDTO(
				"aid",
				"tid",
				listOf(MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now))
			)
		)

		val states = mutableListOf<ChatUIState>()
		val job = launch {
			chatViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()
		chatViewModel.submitNewAdvisory(message)
		advanceUntilIdle()
		job.cancel()

		states shouldContain ChatUIState()
		states shouldContain ChatUIState(
			advisoryId = AdvisoryIdDTO("aid"),
			messages = listOf(MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now)),
			assistantIsTyping = true
		)

		verifySuspend {
			advisorService.initiateAdvisory(AdvisoryMessageRequest(StringMessageDTO(message)))
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `new advisory state changes with error`() = runTest {
		val message = "message"
		val now = Clock.System.now()

		val errorResponse = AeonError(
			correlationId = "49a9c9e6981249a18ba480001007b82f",
			message = "The request has a bad format",
			details = mapOf("email" to "Must be a valid")
		)

		everySuspend { advisorService.initiateAdvisory(any()) } returns AeonErrorResponse(
			statusCode = 500, error = errorResponse, type = ErrorType.SERVER_ERROR
		)

		val states = mutableListOf<ChatUIState>()
		val job = launch {
			chatViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}

		val snacks = mutableListOf<String>()
		val snackjob = launch {
			chatViewModel.snackbarEvent.collect { snack ->
				snacks.add(snack)
			}
		}

		advanceUntilIdle()
		chatViewModel.submitNewAdvisory(message)
		advanceUntilIdle()
		job.cancel()
		snackjob.cancel()

		states shouldContain ChatUIState()
		snacks shouldContain "Failed to initiate advisory"

		chatViewModel.uiState.value.advisoryId shouldBe beNull()
		chatViewModel.uiState.value.messages shouldHaveSize 1
		chatViewModel.uiState.value.messages.first().author shouldBe AuthorDTO.USER
		chatViewModel.uiState.value.messages.first().messageContent shouldBe StringMessageDTO(message)
		chatViewModel.uiState.value.error shouldBe errorResponse.message

		verifySuspend {
			advisorService.initiateAdvisory(AdvisoryMessageRequest(StringMessageDTO(message)))
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `new message state changes`() = runTest {
		val message = "message"
		val newMessage = "message"
		val now = Clock.System.now()

		everySuspend { advisorService.initiateAdvisory(any()) } returns AeonSuccessResponse(
			AdvisoryDTO(
				"aid",
				"tid",
				listOf(MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now))
			)
		)
		everySuspend { advisorService.getAdvisory(any()) } returns AeonSuccessResponse(
			AdvisoryDTO(
				"aid",
				"tid",
				listOf(
					MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now),
					MessageDTO(id = "mID2", messageContent = StringMessageDTO(message), author = AuthorDTO.ASSISTANT, creationDateTime = now + 1.minutes)
				)
			)
		)
		everySuspend { advisorService.addMessage(any(), any()) } returns AeonSuccessResponse(
			MessageDTO(
				"mid3",
				StringMessageDTO(newMessage),
				now + 2.minutes,
				AuthorDTO.USER,
			)
		)

		val states = mutableListOf<ChatUIState>()
		val job = launch {
			chatViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}
		advanceUntilIdle()
		chatViewModel.submitNewAdvisory(message)
		advanceUntilIdle()
		chatViewModel.refreshChat()
		advanceUntilIdle()
		chatViewModel.changeNewMessage(newMessage)
		advanceUntilIdle()
		chatViewModel.submitMessage()
		advanceUntilIdle()
		job.cancel()

		states shouldContain ChatUIState()
		states shouldContain ChatUIState(
			advisoryId = AdvisoryIdDTO("aid"),
			messages = listOf(MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now)),
			assistantIsTyping = true
		)
		states shouldContain ChatUIState(
			advisoryId = AdvisoryIdDTO("aid"),
			messages = listOf(
				MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now),
				MessageDTO(id = "mID2", messageContent = StringMessageDTO(message), author = AuthorDTO.ASSISTANT, creationDateTime = now + 1.minutes)
			),
		)
		states shouldContain ChatUIState(
			advisoryId = AdvisoryIdDTO("aid"),
			messages = listOf(
				MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now),
				MessageDTO(id = "mID2", messageContent = StringMessageDTO(message), author = AuthorDTO.ASSISTANT, creationDateTime = now + 1.minutes)
			),
			newMessage = newMessage
		)
		states shouldContain ChatUIState(
			advisoryId = AdvisoryIdDTO("aid"),
			messages = listOf(
				MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now),
				MessageDTO(id = "mID2", messageContent = StringMessageDTO(message), author = AuthorDTO.ASSISTANT, creationDateTime = now + 1.minutes),
				MessageDTO(
					"mid3",
					StringMessageDTO(newMessage),
					now + 2.minutes,
					AuthorDTO.USER,
				)
			),
			assistantIsTyping = true,
		)
		verifySuspend {
			advisorService.initiateAdvisory(AdvisoryMessageRequest(StringMessageDTO(message)))
			advisorService.getAdvisory(AdvisoryIdDTO("aid"))
			advisorService.addMessage(AdvisoryIdDTO("aid"), AdvisoryMessageRequest(StringMessageDTO(newMessage)))
		}
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun `new message state changes with error`() = runTest {
		val message = "message"
		val newMessage = "message"
		val now = Clock.System.now()

		everySuspend { advisorService.initiateAdvisory(any()) } returns AeonSuccessResponse(
			AdvisoryDTO(
				"aid",
				"tid",
				listOf(MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now))
			)
		)
		everySuspend { advisorService.getAdvisory(any()) } returns AeonSuccessResponse(
			AdvisoryDTO(
				"aid",
				"tid",
				listOf(
					MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now),
					MessageDTO(id = "mID2", messageContent = StringMessageDTO(message), author = AuthorDTO.ASSISTANT, creationDateTime = now + 1.minutes)
				)
			)
		)

		val errorResponse = AeonError(
			correlationId = "49a9c9e6981249a18ba480001007b82f",
			message = "The request has a bad format",
			details = mapOf("email" to "Must be a valid")
		)

		everySuspend { advisorService.addMessage(any(), any()) } returns AeonErrorResponse(
			statusCode = 500, error = errorResponse, type = ErrorType.SERVER_ERROR
		)

		val states = mutableListOf<ChatUIState>()
		val job = launch {
			chatViewModel.uiState.collect { uiStates ->
				states.add(uiStates)
			}
		}

		val snacks = mutableListOf<String>()
		val snackjob = launch {
			chatViewModel.snackbarEvent.collect { snack ->
				snacks.add(snack)
			}
		}

		advanceUntilIdle()
		chatViewModel.submitNewAdvisory(message)
		advanceUntilIdle()
		chatViewModel.refreshChat()
		advanceUntilIdle()
		chatViewModel.changeNewMessage(newMessage)
		advanceUntilIdle()
		chatViewModel.submitMessage()
		advanceUntilIdle()
		job.cancel()
		snackjob.cancel()

		states shouldContain ChatUIState()
		states shouldContain ChatUIState(
			advisoryId = AdvisoryIdDTO("aid"),
			messages = listOf(MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now)),
			assistantIsTyping = true
		)
		states shouldContain ChatUIState(
			advisoryId = AdvisoryIdDTO("aid"),
			messages = listOf(
				MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now),
				MessageDTO(id = "mID2", messageContent = StringMessageDTO(message), author = AuthorDTO.ASSISTANT, creationDateTime = now + 1.minutes)
			),
		)
		states shouldContain ChatUIState(
			advisoryId = AdvisoryIdDTO("aid"),
			messages = listOf(
				MessageDTO(id = "mID", messageContent = StringMessageDTO(message), author = AuthorDTO.USER, creationDateTime = now),
				MessageDTO(id = "mID2", messageContent = StringMessageDTO(message), author = AuthorDTO.ASSISTANT, creationDateTime = now + 1.minutes)
			),
			newMessage = newMessage
		)
		snacks shouldContain "Failed to add message to advisory"

		chatViewModel.uiState.value.messages shouldHaveSize 3
		chatViewModel.uiState.value.messages.last().id shouldBe "new"
		chatViewModel.uiState.value.messages.last().author shouldBe AuthorDTO.USER
		chatViewModel.uiState.value.messages.last().messageContent shouldBe StringMessageDTO(newMessage)
		chatViewModel.uiState.value.error shouldBe errorResponse.message
		
		verifySuspend {
			advisorService.initiateAdvisory(AdvisoryMessageRequest(StringMessageDTO(message)))
			advisorService.getAdvisory(AdvisoryIdDTO("aid"))
			advisorService.addMessage(AdvisoryIdDTO("aid"), AdvisoryMessageRequest(StringMessageDTO(newMessage)))
		}
	}

}