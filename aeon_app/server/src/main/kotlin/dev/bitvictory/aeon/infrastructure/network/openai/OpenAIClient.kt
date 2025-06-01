package dev.bitvictory.aeon.infrastructure.network.openai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.assistant.AssistantRequest
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.run.RunId
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.api.run.ThreadRunRequest
import com.aallam.openai.api.thread.ThreadId
import com.aallam.openai.api.thread.ThreadMessage
import com.aallam.openai.api.thread.ThreadRequest
import com.aallam.openai.client.OpenAI
import dev.bitvictory.aeon.core.domain.entities.assistant.AeonAssistant
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutput
import dev.bitvictory.aeon.core.domain.entities.system.SystemComponentHealth
import dev.bitvictory.aeon.core.domain.usecases.system.SystemComponentHealthProvider
import dev.bitvictory.aeon.infrastructure.network.dto.AssistantMessageDTO
import dev.bitvictory.aeon.infrastructure.network.mapper.toOpenAI
import dev.bitvictory.aeon.model.primitive.UptimeStatus
import io.ktor.util.logging.KtorSimpleLogger

@OptIn(BetaOpenAI::class)
class OpenAIClient(private val openAI: OpenAI): SystemComponentHealthProvider {

	private val logger = KtorSimpleLogger(this.javaClass.name)

	override fun getName(): String = "OpenAI"

	suspend fun executeThread(assistantId: AssistantId, initialMessage: AssistantMessageDTO) = openAI.createStreamingThreadRun(
		request = ThreadRunRequest(
			assistantId = assistantId,
			thread = ThreadRequest(
				messages = listOf(
					ThreadMessage(
						role = initialMessage.externalRole(),
						content = initialMessage.content
					)
				)
			),
		)
	)

	suspend fun run(assistantId: AssistantId, threadId: String) = openAI.createStreamingRun(ThreadId(threadId), RunRequest(assistantId))

	suspend fun writeMessageToThread(threadId: String, message: AssistantMessageDTO) =
		openAI.message(
			threadId = ThreadId(threadId),
			request = MessageRequest(
				role = message.externalRole(),
				content = message.content
			)
		).id

	suspend fun submitActionResult(threadId: String, runId: String, toolOutput: List<AeonToolOutput>) =
		openAI.submitStreamingToolOutput(ThreadId(threadId), RunId(runId), toolOutput.map { it.toOpenAI() })

	suspend fun assistants() = openAI.assistants()

	suspend fun createAssistant(aeonAssistant: AeonAssistant) = openAI.assistant(
		request = AssistantRequest(
			name = aeonAssistant.name,
			instructions = aeonAssistant.instructions,
			model = ModelId(aeonAssistant.model),
			tools = aeonAssistant.tools.map { it.toOpenAI() },
			metadata = mapOf("version" to aeonAssistant.version)
		)
	)

	suspend fun deleteAssistant(assistantId: AssistantId) = openAI.delete(assistantId)

	override suspend fun getHealth(): SystemComponentHealth {
		logger.debug("Checking OpenAI health")
		try {
			val models = openAI.models()
			return if (models.isNotEmpty()) {
				SystemComponentHealth(getName(), UptimeStatus.UP)
			} else {
				SystemComponentHealth(getName(), UptimeStatus.DOWN, "No models found")
			}
		} catch (e: Exception) {
			logger.error("Error when checking OpenAI health.", e)
			return SystemComponentHealth(getName(), UptimeStatus.DOWN, "Error when checking OpenAI health. ${e.message}")
		}
	}
}