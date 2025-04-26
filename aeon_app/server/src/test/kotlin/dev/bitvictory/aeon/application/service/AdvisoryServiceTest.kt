package dev.bitvictory.aeon.application.service

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.message.MessageId
import com.aallam.openai.api.run.RunId
import com.aallam.openai.api.thread.ThreadId
import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.advisory.StringMessage
import dev.bitvictory.aeon.core.domain.entities.advisory.ThreadContext
import dev.bitvictory.aeon.core.domain.entities.assistant.Author
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import dev.bitvictory.aeon.core.domain.usecases.assistant.AssistantExecution
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

@OptIn(BetaOpenAI::class)
@ExtendWith(MockKExtension::class)
class AdvisoryServiceTest {

	@MockK
	lateinit var advisoryPersistence: AdvisoryPersistence

	@MockK
	lateinit var assistantExecution: AssistantExecution

	lateinit var advisoryService: AdvisoryService

	@OptIn(ExperimentalCoroutinesApi::class)
	@BeforeTest
	fun setup() {
		Dispatchers.setMain(UnconfinedTestDispatcher())
		advisoryService = AdvisoryService(advisoryPersistence, assistantExecution, false)
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
			val threadId = "tId"
			val runId = "rId"
			val message = Message(messageContent = StringMessage("Hello"), author = Author.USER, user = user, creationDateTime = now)
			val messageHandled = Message(messageContent = StringMessage("Hello"), author = Author.USER, user = user, creationDateTime = now, messageId = "mid")
			val messageExecuted =
				Message(messageContent = StringMessage("Hello"), author = Author.USER, user = user, creationDateTime = now, messageId = "mid", runId = runId)

			coEvery { assistantExecution.initiateThread(any()) } returns ThreadId(threadId)
			coEvery { assistantExecution.fetchMessages(threadId, null, user) } returns listOf(messageHandled)
			coEvery { assistantExecution.executeAssistant(threadId) } returns RunId(runId)
			coEvery { advisoryPersistence.insert(any()) } returns Unit

			advisoryService.startNewAdvisory(message)

			coVerify {
				assistantExecution.initiateThread(
					AssistantMessageDTO(
						message.author,
						"Hello"
					)
				)
			}
			coVerify { assistantExecution.fetchMessages(threadId, null, user) }
			coVerify { assistantExecution.executeAssistant(threadId) }

			val advisorySlot = slot<Advisory>()
			coVerify { advisoryPersistence.insert(capture(advisorySlot)) }
			advisorySlot.captured.user shouldBe user
			advisorySlot.captured.messages shouldBe listOf(messageExecuted)
			advisorySlot.captured.threadId shouldBe threadId
		}

	}

	@Nested
	inner class ExistingAdvisories {
		@Test
		fun `add message existing advisory fetches messages and starts execution`(@MockK user: User) = runTest {
			val now = Clock.System.now()
			val advisoryId = ObjectId.get()
			val threadId = "tId"
			val messageId = "mId"
			val runId = "rId"
			val message = Message(messageContent = StringMessage("Hello"), author = Author.USER, user = user, creationDateTime = now)
			val messageExecuted =
				Message(
					messageContent = StringMessage("Hello"),
					author = Author.USER,
					user = user,
					creationDateTime = now,
					messageId = messageId,
					runId = runId
				)

			coEvery { advisoryPersistence.getThreadContext(any()) } returns ThreadContext(threadId, user)
			coEvery { assistantExecution.writeMessageToThread(threadId, any(AssistantMessageDTO::class)) } returns MessageId(messageId)
			coEvery { assistantExecution.executeAssistant(threadId) } returns RunId(runId)
			coEvery { advisoryPersistence.appendMessages(any(), any()) } returns Unit

			advisoryService.addMessage(advisoryId, message)

			coVerify { advisoryPersistence.getThreadContext(advisoryId) }
			coVerify {
				assistantExecution.writeMessageToThread(
					threadId, AssistantMessageDTO(
						message.author, "Hello"
					)
				)
			}
			coVerify { assistantExecution.executeAssistant(threadId) }

			val advisorySlot = slot<List<Message>>()
			coVerify { advisoryPersistence.appendMessages(advisoryId, capture(advisorySlot)) }
			advisorySlot.captured shouldHaveSize 1
			advisorySlot.captured.first() shouldBe messageExecuted
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
			coVerify(exactly = 0) { assistantExecution.writeMessageToThread(any(), any()) }
			coVerify(exactly = 0) { assistantExecution.executeAssistant(threadId) }

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
			val advisory = Advisory(advisoryId, "tId", user, listOf())


			coEvery { advisoryPersistence.getById(any()) } returns advisory

			val advisoryDB = advisoryService.retrieveAdvisoryById(advisoryId, user)

			coVerify { advisoryPersistence.getById(advisoryId) }
			advisoryDB shouldBe advisory
		}

		@Test
		fun `get existing advisory with different user`(@MockK user: User) = runTest {
			val advisoryId = ObjectId.get()
			val advisory = Advisory(advisoryId, "tId", user, listOf())


			coEvery { advisoryPersistence.getById(any()) } returns advisory

			shouldThrow<AuthorizationException> { advisoryService.retrieveAdvisoryById(advisoryId, mockk()) }
		}
	}

}