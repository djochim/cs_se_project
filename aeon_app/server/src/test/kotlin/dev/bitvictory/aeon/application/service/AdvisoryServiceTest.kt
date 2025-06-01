package dev.bitvictory.aeon.application.service

import dev.bitvictory.aeon.application.service.actioncall.ActionCallDispatcher
import dev.bitvictory.aeon.application.service.eventhandling.AdvisoryEventDispatcher
import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.advisory.StringMessage
import dev.bitvictory.aeon.core.domain.entities.advisory.ThreadContext
import dev.bitvictory.aeon.core.domain.entities.assistant.HealthAssistant
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCallWrapper
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonActionCalls
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonFunctionCall
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutput
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutputs
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonAssistantEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AeonMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.message.Author
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import dev.bitvictory.aeon.core.domain.usecases.assistant.AsyncAssistantExecution
import dev.bitvictory.aeon.exceptions.AuthorizationException
import dev.bitvictory.aeon.infrastructure.network.dto.AssistantMessageDTO
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class AdvisoryServiceTest {

	@MockK
	lateinit var advisoryPersistence: AdvisoryPersistence

	@MockK
	lateinit var asyncAssistantExecution: AsyncAssistantExecution

	@MockK
	lateinit var advisoryEventDispatcher: AdvisoryEventDispatcher

	@MockK
	lateinit var actionCallDispatcher: ActionCallDispatcher

	lateinit var advisoryService: AdvisoryService

	@OptIn(ExperimentalCoroutinesApi::class)
	@BeforeTest
	fun setup() {
		val testDispatcher = UnconfinedTestDispatcher()
		Dispatchers.setMain(testDispatcher)
		advisoryService = AdvisoryService(advisoryPersistence, asyncAssistantExecution, advisoryEventDispatcher, actionCallDispatcher, testDispatcher)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@AfterTest
	fun tearDown() {
		Dispatchers.resetMain()
	}

	@Nested
	inner class NewAdvisories {

		@Test
		fun `new advisory creates new thread and starts execution`(@MockK user: User) = runTest {
			val now = Clock.System.now()
			val message = Message(messageContent = StringMessage("Hello"), author = Author.USER, user = user, creationDateTime = now)

			val event1 = mockk<AeonAssistantEvent>()
			val event2 = mockk<AeonAssistantEvent>()
			val event3 = mockk<AeonAssistantEvent>()

			val emptyActionCallWrapper = AeonActionCallWrapper.empty()

			coEvery { asyncAssistantExecution.executeThread(any(), any()) } returns flowOf(event1, event2, event3)
			coEvery { advisoryEventDispatcher.dispatch(any(), any()) } returns emptyActionCallWrapper
			coEvery { advisoryPersistence.insert(any()) } returns Unit

			advisoryService.startNewAdvisory(message)

			coVerify {
				asyncAssistantExecution.executeThread(
					HealthAssistant,
					AssistantMessageDTO(
						message.author,
						"Hello"
					)
				)
			}
			coVerify(exactly = 3) { advisoryEventDispatcher.dispatch(any(), any()) }
			coVerify(exactly = 0) { actionCallDispatcher.dispatch(any(), any(), any()) }
			coVerify(exactly = 0) { asyncAssistantExecution.submitActionResult(any(), any(), any()) }

			val advisorySlot = slot<Advisory>()
			coVerify { advisoryPersistence.insert(capture(advisorySlot)) }
			advisorySlot.captured.user shouldBe user
			advisorySlot.captured.threadId shouldBe ""
		}

	}

	@Nested
	inner class ExistingAdvisories {
		@Test
		fun `add message existing advisory fetches messages and starts execution`(@MockK user: User) = runTest {
			val now = Clock.System.now()
			val advisoryId = ObjectId.get()
			val threadId = "tId"
			val runId = "rId"
			val message = Message(messageContent = StringMessage("Hello"), author = Author.USER, user = user, creationDateTime = now)

			val event1 = mockk<AeonAssistantEvent>()
			val event2 = mockk<AeonAssistantEvent>()
			val event3 = mockk<AeonAssistantEvent>()

			val emptyActionCallWrapper = AeonActionCallWrapper.empty()
			val actionCallWrapper = AeonActionCallWrapper(threadId, runId, AeonActionCalls(listOf(AeonFunctionCall("cId", "name", "args"))))
			val toolOutputs = AeonToolOutputs(threadId, runId, listOf(AeonToolOutput("tId", "output")))

			coEvery { advisoryPersistence.getThreadContext(any()) } returns ThreadContext(threadId, user)
			coEvery { asyncAssistantExecution.writeMessageToThread(any(), any(), any()) } returns flowOf(event1, event2, event3)
			coEvery { advisoryEventDispatcher.dispatch(any(), eq(event3, inverse = true)) } returns emptyActionCallWrapper
			coEvery { advisoryEventDispatcher.dispatch(any(), eq(event3)) } returns actionCallWrapper
			coEvery { actionCallDispatcher.dispatch(any(), any(), any()) } returns toolOutputs
			coEvery { asyncAssistantExecution.submitActionResult(any(), any(), any()) } returns flowOf(event1, event2)
			coEvery { advisoryPersistence.appendMessages(any(), any()) } returns Unit

			advisoryService.addMessage(advisoryId, message)

			coVerify { advisoryPersistence.getThreadContext(advisoryId) }
			coVerify {
				asyncAssistantExecution.writeMessageToThread(
					HealthAssistant, threadId, AssistantMessageDTO(
						message.author, "Hello"
					)
				)
			}
			coVerify(exactly = 5) { advisoryEventDispatcher.dispatch(any(), any()) }
			coVerify(exactly = 1) { actionCallDispatcher.dispatch(any(), any(), any()) }
			coVerify(exactly = 1) { asyncAssistantExecution.submitActionResult(any(), any(), any()) }

			val advisorySlot = slot<List<AeonMessage>>()
			coVerify { advisoryPersistence.appendMessages(advisoryId, capture(advisorySlot)) }
			advisorySlot.captured shouldHaveSize 1
		}

		@Test
		fun `add message existing advisory with different user fails`(@MockK user: User) = runTest {
			val now = Clock.System.now()
			val advisoryId = ObjectId.get()
			val threadId = "tId"
			val message = Message(messageContent = StringMessage("Hello"), author = Author.USER, user = user, creationDateTime = now)

			coEvery { advisoryPersistence.getThreadContext(any()) } returns ThreadContext(threadId, mockk())

			shouldThrow<AuthorizationException> { advisoryService.addMessage(advisoryId, message) }

			coVerify { advisoryPersistence.getThreadContext(advisoryId) }
			coVerify(exactly = 0) { asyncAssistantExecution.writeMessageToThread(any(), any(), any()) }
			coVerify(exactly = 0) { advisoryPersistence.appendMessages(any(), any()) }
		}

		@Test
		fun `fetch new messages`(@MockK user: User) = runTest {
			val now = Clock.System.now()
			val advisoryId = ObjectId.get()
			val messageId = "mId"
			val runId = "rId"
			val messageExecuted =
				Message(
					messageContent = StringMessage("Hello"),
					author = Author.USER,
					user = user,
					creationDateTime = now,
					messageId = messageId,
					runId = runId
				)

			coEvery { advisoryPersistence.getNewMessages(any(), any()) } returns listOf(messageExecuted)

			val messages = advisoryService.retrieveMessages(advisoryId, now)

			coVerify { advisoryPersistence.getNewMessages(advisoryId, now) }
			messages shouldHaveSize 1
			messages.first() shouldBe messageExecuted
		}

		@Test
		fun `get existing advisory`(@MockK user: User) = runTest {
			val advisoryId = ObjectId.get()
			val advisory = Advisory(advisoryId, "tId", user, AeonStatus.UNKNOWN, listOf(), listOf())


			coEvery { advisoryPersistence.getById(any()) } returns advisory

			val advisoryDB = advisoryService.retrieveAdvisoryById(advisoryId, user)

			coVerify { advisoryPersistence.getById(advisoryId) }
			advisoryDB shouldBe advisory
		}

		@Test
		fun `get existing advisory with different user`(@MockK user: User) = runTest {
			val advisoryId = ObjectId.get()
			val advisory = Advisory(advisoryId, "tId", user, AeonStatus.UNKNOWN, listOf(), listOf())


			coEvery { advisoryPersistence.getById(any()) } returns advisory

			shouldThrow<AuthorizationException> { advisoryService.retrieveAdvisoryById(advisoryId, mockk()) }
		}
	}
}