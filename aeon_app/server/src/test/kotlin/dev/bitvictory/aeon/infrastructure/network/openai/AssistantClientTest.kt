package dev.bitvictory.aeon.infrastructure.network.openai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.Assistant
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.assistant.AssistantRequest
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.message.Message
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.message.MessageId
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.message.TextContent
import com.aallam.openai.api.run.Run
import com.aallam.openai.api.run.RunId
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.api.thread.Thread
import com.aallam.openai.api.thread.ThreadId
import com.aallam.openai.api.thread.ThreadMessage
import com.aallam.openai.api.thread.ThreadRequest
import com.aallam.openai.client.OpenAI
import dev.bitvictory.aeon.core.domain.entities.advisory.StringMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.Author
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.infrastructure.network.dto.AssistantMessageDTO
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.beEmpty
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

@OptIn(BetaOpenAI::class)
@ExtendWith(MockKExtension::class)
class AssistantClientTest {

	@MockK
	lateinit var openAI: OpenAI

	@MockK
	lateinit var assistant: Assistant

	val assistantName: String = "assistant"
	val assistantId: String = "aId"

	lateinit var assistantClient: AssistantClient

	@BeforeEach
	fun setup() {
		coEvery { openAI.assistants(limit = 5) } returns listOf(assistant)
		every { assistant.name } returns assistantName
		every { assistant.id } returns AssistantId(assistantId)
		assistantClient = AssistantClient(openAI, assistantName)
	}

	@Nested
	inner class AssistantManagement {

		@Test
		fun `Successful create new assistant when it does not exist`() = runTest {
			coEvery { openAI.assistants(limit = any()) } returns listOf(assistant)
			every { assistant.name } returns "other"

			val newAssistant = mockk<Assistant>()
			coEvery { openAI.assistant(request = any(AssistantRequest::class)) } returns newAssistant

			AssistantClient(openAI, assistantName)

			val assistantRequestSlot = slot<AssistantRequest>()
			coVerify {
				openAI.assistant(request = capture(assistantRequestSlot))
			}

			assistantRequestSlot.captured.name shouldBe assistantName
			assistantRequestSlot.captured.instructions shouldNotBe beEmpty()
			assistantRequestSlot.captured.model?.id shouldBe "gpt-4o"
		}

	}

	@Nested
	inner class Threads {
		@Test
		fun `Successful init new thread`() = runTest {
			val initialMessage = AssistantMessageDTO(Author.USER, "Hello")

			val thread = mockk<Thread>()
			every { thread.id } returns ThreadId("thread")
			coEvery { openAI.thread(request = any(ThreadRequest::class)) } returns thread

			val threadId = assistantClient.initiateThread(initialMessage)

			threadId shouldBe ThreadId("thread")

			coVerify {
				openAI.thread(
					request = ThreadRequest(
						messages = listOf(
							ThreadMessage(
								role = Role.User,
								content = "Hello"
							)
						)
					)
				)
			}
		}

		@Test
		fun `Successfully adding messages to the thread`() = runTest {
			val threadId = "tid"
			val assistantMessage = AssistantMessageDTO(Author.USER, "Hello")

			val message = mockk<Message>()
			every { message.id } returns MessageId("mid")
			coEvery { openAI.message(threadId = any(), request = any(MessageRequest::class)) } returns message

			val messageId = assistantClient.writeMessageToThread(threadId, assistantMessage)

			messageId shouldBe MessageId("mid")

			val messageSlot = slot<MessageRequest>()
			coVerify {
				openAI.message(
					threadId = ThreadId(threadId),
					request = capture(messageSlot)
				)
			}

			messageSlot.captured.role shouldBe Role.User
			messageSlot.captured.content shouldBe "Hello"
		}

		@Test
		fun `Successfully adding messages to the thread with different role then user`() = runTest {
			val threadId = "tid"
			val assistantMessage = AssistantMessageDTO(Author.SYSTEM, "Hello")

			val message = mockk<Message>()
			every { message.id } returns MessageId("mid")
			coEvery { openAI.message(threadId = any(), request = any(MessageRequest::class)) } returns message

			val messageId = assistantClient.writeMessageToThread(threadId, assistantMessage)

			messageId shouldBe MessageId("mid")

			val messageSlot = slot<MessageRequest>()
			coVerify {
				openAI.message(
					threadId = ThreadId(threadId),
					request = capture(messageSlot)
				)
			}
			messageSlot.captured.role shouldBe Role.System
			messageSlot.captured.content shouldBe "Hello"
		}
	}

	@Nested
	inner class RunExecution {
		@Test
		fun `Successfully execute the assistant`() = runTest {
			val threadId = "tid"

			val run = mockk<Run>()
			every { run.id } returns RunId("rid")
			coEvery { openAI.createRun(threadId = any(), request = any(RunRequest::class)) } returns run

			val runId = assistantClient.executeAssistant(threadId)

			runId shouldBe RunId("rid")

			coVerify {
				openAI.createRun(
					threadId = ThreadId(threadId),
					request = RunRequest(
						assistantId = AssistantId(assistantId)
					)
				)
			}
		}

		@Test
		fun `Successfully fetch new messages with older message`() = runTest {
			val threadId = "tid"
			val olderMessageId = "omid"
			val user = mockk<User>()

			val now = Clock.System.now()

			val message1 = mockMessage("mid", now.minus(5.minutes), Role.User, "Hello", "rid")
			val message2 = mockMessage("mid2", now.minus(10.minutes), Role.System, "Hello World", "rid2")
			val messages = listOf(message1, message2)
			coEvery { openAI.messages(threadId = any(), before = any(MessageId::class)) } returns messages

			val result = assistantClient.fetchMessages(threadId, olderMessageId, user)

			result shouldHaveSize 2
			result shouldContain dev.bitvictory.aeon.core.domain.entities.advisory.Message(
				creationDateTime = now.minus(5.minutes).withoutMilliseconds(),
				messageId = "mid",
				author = Author.USER,
				user = user,
				messageContent = StringMessage("Hello"),
				runId = "rid"
			)
			result shouldContain dev.bitvictory.aeon.core.domain.entities.advisory.Message(
				creationDateTime = now.minus(10.minutes).withoutMilliseconds(),
				messageId = "mid2",
				author = Author.SYSTEM,
				user = user,
				messageContent = StringMessage("Hello World"),
				runId = "rid2"
			)

			coVerify {
				openAI.messages(
					threadId = ThreadId(threadId),
					before = MessageId(olderMessageId)
				)
			}
		}

		private fun Instant.withoutMilliseconds(): Instant {
			val secondsSinceEpoch = this.epochSeconds
			return Instant.fromEpochSeconds(secondsSinceEpoch)
		}

		private fun mockMessage(id: String, createdAt: Instant, role: Role, content: String, runId: String): Message {
			val message = mockk<Message>()
			every { message.id } returns MessageId(id)
			every { message.createdAt } returns createdAt.epochSeconds.toInt()
			every { message.role } returns role
			every { message.content } returns listOf(MessageContent.Text(TextContent(content, emptyList())))
			every { message.runId } returns RunId(runId)

			return message
		}
	}

}