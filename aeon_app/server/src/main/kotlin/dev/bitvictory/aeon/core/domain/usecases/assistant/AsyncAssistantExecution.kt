package dev.bitvictory.aeon.core.domain.usecases.assistant

import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.assistant.AeonAssistant
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutput
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonAssistantEvent
import kotlinx.coroutines.flow.Flow

interface AsyncAssistantExecution {
    suspend fun executeThread(
        aeonAssistant: AeonAssistant,
        initialMessage: Message
    ): Flow<AeonAssistantEvent>

    suspend fun submitActionResult(
        threadId: String,
        runId: String,
        toolOutput: List<AeonToolOutput>
    ): Flow<AeonAssistantEvent>

    suspend fun writeMessageToThread(
        aeonAssistant: AeonAssistant,
        threadId: String,
        message: Message
    ): Flow<AeonAssistantEvent>
}