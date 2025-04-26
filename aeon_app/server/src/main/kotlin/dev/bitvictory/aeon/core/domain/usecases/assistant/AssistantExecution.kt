package dev.bitvictory.aeon.core.domain.usecases.assistant

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.message.MessageId
import com.aallam.openai.api.run.RunId
import com.aallam.openai.api.thread.ThreadId
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.assistant.RunState
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.infrastructure.network.dto.AssistantMessageDTO

@OptIn(BetaOpenAI::class)
interface AssistantExecution {
	suspend fun executeAssistant(thread: String): RunId
	suspend fun fetchRun(thread: String, run: String): RunState

	suspend fun writeMessageToThread(thread: String, message: AssistantMessageDTO): MessageId
	suspend fun fetchMessages(thread: String, olderMessage: String?, user: User): List<Message>

	suspend fun initiateThread(initialMessage: AssistantMessageDTO): ThreadId
}