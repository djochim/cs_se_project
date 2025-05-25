package dev.bitvictory.aeon.infrastructure.network.openai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantId
import dev.bitvictory.aeon.core.domain.entities.assistant.AeonAssistant
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutput
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonAssistantEvent
import dev.bitvictory.aeon.core.domain.usecases.assistant.AsyncAssistantExecution
import dev.bitvictory.aeon.infrastructure.network.dto.AssistantMessageDTO
import dev.bitvictory.aeon.infrastructure.network.mapper.toAeonAssistantEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(BetaOpenAI::class)
class AssistantService(private val openAIClient: OpenAIClient, aeonAssistants: List<AeonAssistant>): AsyncAssistantExecution {

	@OptIn(DelicateCoroutinesApi::class)
	private val assistants = GlobalScope.async { initAssistant(aeonAssistants) }

	override suspend fun executeThread(aeonAssistant: AeonAssistant, initialMessage: AssistantMessageDTO): Flow<AeonAssistantEvent> =
		openAIClient.executeThread(assistant(aeonAssistant), initialMessage).map { it.toAeonAssistantEvent() }

	override suspend fun submitActionResult(threadId: String, runId: String, toolOutput: List<AeonToolOutput>): Flow<AeonAssistantEvent> {
		return openAIClient.submitActionResult(threadId, runId, toolOutput).map { it.toAeonAssistantEvent() }
	}

	override suspend fun writeMessageToThread(aeonAssistant: AeonAssistant, threadId: String, message: AssistantMessageDTO): Flow<AeonAssistantEvent> {
		openAIClient.writeMessageToThread(threadId, message)
		return openAIClient.run(assistant(aeonAssistant), threadId).map { it.toAeonAssistantEvent() }
	}

	private suspend fun assistant(aeonAssistant: AeonAssistant) =
		assistants.await()[aeonAssistant.name] ?: throw IllegalStateException("Assistant ${aeonAssistant.name} not found")

	private suspend fun initAssistant(aeonAssistants: List<AeonAssistant>): Map<String, AssistantId> {
		val existingAssistant = openAIClient.assistants()
		return aeonAssistants.associate { aeonAssistant ->
			val existing = existingAssistant.firstOrNull { it.name == aeonAssistant.name }
			if (existing == null || existing.tools.isEmpty()) {
				aeonAssistant.name to openAIClient.createAssistant(aeonAssistant).id
			} else {
				aeonAssistant.name to existing.id
			}
		}
	}

}