package dev.bitvictory.aeon.core.domain.usecases.assistant

import dev.bitvictory.aeon.core.domain.entities.assistant.AeonAssistant
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutput
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonAssistantEvent
import dev.bitvictory.aeon.infrastructure.network.dto.AssistantMessageDTO
import kotlinx.coroutines.flow.Flow

interface AsyncAssistantExecution {
	suspend fun executeThread(aeonAssistant: AeonAssistant, initialMessage: AssistantMessageDTO): Flow<AeonAssistantEvent>
	suspend fun submitActionResult(threadId: String, runId: String, toolOutput: List<AeonToolOutput>): Flow<AeonAssistantEvent>
	suspend fun writeMessageToThread(aeonAssistant: AeonAssistant, threadId: String, message: AssistantMessageDTO): Flow<AeonAssistantEvent>
}