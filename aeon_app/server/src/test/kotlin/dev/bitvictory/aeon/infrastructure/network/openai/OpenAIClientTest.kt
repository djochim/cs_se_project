package dev.bitvictory.aeon.infrastructure.network.openai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.chat.ToolId
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.message.Message
import com.aallam.openai.api.message.MessageId
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.run.AssistantStreamEvent
import com.aallam.openai.api.run.AssistantStreamEventType
import com.aallam.openai.api.run.Run
import com.aallam.openai.api.run.RunId
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.api.run.ThreadRunRequest
import com.aallam.openai.api.run.ToolOutput
import com.aallam.openai.api.thread.Thread
import com.aallam.openai.api.thread.ThreadId
import com.aallam.openai.api.thread.ThreadMessage
import com.aallam.openai.api.thread.ThreadRequest
import com.aallam.openai.client.OpenAI
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutput
import dev.bitvictory.aeon.core.domain.entities.assistant.message.Author
import dev.bitvictory.aeon.core.domain.entities.system.SystemComponentHealth
import dev.bitvictory.aeon.infrastructure.network.dto.AssistantMessageDTO
import dev.bitvictory.aeon.model.primitive.UptimeStatus
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
@OptIn(BetaOpenAI::class)
class OpenAIClientTest {

	@MockK
	lateinit var openAI: OpenAI

	@InjectMockKs
	lateinit var openAIClient: OpenAIClient

	@OptIn(BetaOpenAI::class)
	val assistantId = AssistantId("aId")

	@Nested
	inner class HealthCheckTests {
		@Test
		fun `Successful health check with models`() {
			runBlocking {
				val model = mockk<Model>()
				val models = listOf(model)
				coEvery { openAI.models() } returns models

				val result = openAIClient.getHealth()

				coVerify {
					openAI.models()
				}
				result shouldBe SystemComponentHealth(openAIClient.getName(), UptimeStatus.UP)
			}
		}

		@Test
		fun `Successful health check with no models result in downtime`() {
			runBlocking {
				val models = listOf<Model>()
				coEvery { openAI.models() } returns models

				val result = openAIClient.getHealth()

				coVerify {
					openAI.models()
				}
				result.status shouldBe UptimeStatus.DOWN
			}
		}

		@Test
		fun `API call failure`() {
			runBlocking {
				coEvery { openAI.models() } throws Exception("API call failed")

				val result = openAIClient.getHealth()

				result.status shouldBe UptimeStatus.DOWN
			}
		}
	}

	@Nested
	inner class Threads {
		@Test
		fun `Successful init new thread`() = runTest {
			val initialMessage = AssistantMessageDTO(Author.USER, "Hello")

			val thread = mockk<Thread>()
			val oaEvent = AssistantStreamEvent(
				"event",
				AssistantStreamEventType.THREAD_CREATED,
				"data"
			)
			every { thread.id } returns ThreadId("thread")
			coEvery { openAI.createStreamingThreadRun(request = any(ThreadRunRequest::class)) } returns flowOf(
				oaEvent
			)

			val threadFlow = openAIClient.executeThread(assistantId, initialMessage)

			val events = ArrayList<AssistantStreamEvent>()
			val job = threadFlow.onEach {
				events.add(it)
			}.launchIn(this)

			job.join()

			coVerify {
				openAI.createStreamingThreadRun(
					request = ThreadRunRequest(
						assistantId = assistantId,
						thread = ThreadRequest(
							messages = listOf(
								ThreadMessage(
									role = Role.User,
									content = "Hello"
								)
							)
						)
					)
				)
			}
			events shouldHaveSize 1
			events[0] shouldBe oaEvent
		}

		@Test
		fun `Successfully adding messages to the thread`() = runTest {
			val threadId = "tid"
			val assistantMessage = AssistantMessageDTO(Author.USER, "Hello")

			val message = mockk<Message>()
			every { message.id } returns MessageId("mid")
			coEvery { openAI.message(threadId = any(), request = any(MessageRequest::class)) } returns message

			val messageId = openAIClient.writeMessageToThread(threadId, assistantMessage)

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

			val messageId = openAIClient.writeMessageToThread(threadId, assistantMessage)

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
			val oaEvent = AssistantStreamEvent(
				"event",
				AssistantStreamEventType.THREAD_RUN_CREATED,
				"data"
			)
			coEvery { openAI.createStreamingRun(threadId = any(), request = any(RunRequest::class)) } returns flowOf(oaEvent)

			val events = ArrayList<AssistantStreamEvent>()
			val job = openAIClient.run(assistantId, threadId).onEach { events.add(it) }.launchIn(this)

			job.join()

			events shouldHaveSize 1
			events[0] shouldBe oaEvent

			coVerify {
				openAI.createStreamingRun(
					threadId = ThreadId(threadId),
					request = RunRequest(
						assistantId = assistantId
					)
				)
			}
		}

		@Test
		fun `Successfully submit tool output to the assistant`() = runTest {
			val threadId = ThreadId("tid")

			val runId = RunId("rid")
			val oaEvent = AssistantStreamEvent(
				"event",
				AssistantStreamEventType.THREAD_RUN_COMPLETED,
				"data"
			)
			coEvery { openAI.submitStreamingToolOutput(threadId = any(), runId = any(), output = any()) } returns flowOf(oaEvent)

			val events = ArrayList<AssistantStreamEvent>()
			val job = openAIClient.submitActionResult(threadId.id, runId.id, listOf(AeonToolOutput("id", "output"))).onEach { events.add(it) }.launchIn(this)

			job.join()

			events shouldHaveSize 1
			events[0] shouldBe oaEvent

			coVerify {
				openAI.submitStreamingToolOutput(
					threadId = threadId,
					runId = runId,
					output = listOf(ToolOutput(ToolId("id"), "output"))
				)
			}
		}

	}

}