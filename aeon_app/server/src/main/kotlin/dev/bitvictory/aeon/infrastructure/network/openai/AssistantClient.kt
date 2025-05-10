package dev.bitvictory.aeon.infrastructure.network.openai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.Assistant
import com.aallam.openai.api.assistant.AssistantRequest
import com.aallam.openai.api.message.MessageId
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.run.RunId
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.api.thread.ThreadId
import com.aallam.openai.api.thread.ThreadMessage
import com.aallam.openai.api.thread.ThreadRequest
import com.aallam.openai.client.OpenAI
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.assistant.RunState
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.core.domain.usecases.assistant.AssistantExecution
import dev.bitvictory.aeon.infrastructure.network.dto.AssistantMessageDTO
import dev.bitvictory.aeon.infrastructure.network.mapper.toAssistantMessage
import dev.bitvictory.aeon.infrastructure.network.mapper.toAuthor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.datetime.Instant

@OptIn(BetaOpenAI::class)
class AssistantClient(private val openAI: OpenAI, private val assistantName: String):
	AssistantExecution {

	@OptIn(DelicateCoroutinesApi::class)
	private val assistant = GlobalScope.async { initAssistant() }

	override suspend fun initiateThread(initialMessage: AssistantMessageDTO) = openAI.thread(
		request = ThreadRequest(
			messages = listOf(
				ThreadMessage(
					role = initialMessage.externalRole(),
					content = initialMessage.content
				)
			)
		)
	).id

	override suspend fun writeMessageToThread(thread: String, message: AssistantMessageDTO) =
		openAI.message(
			threadId = ThreadId(thread),
			request = MessageRequest(
				role = message.externalRole(),
				content = message.content
			)
		).id

	override suspend fun executeAssistant(thread: String) = openAI.createRun(
		threadId = ThreadId(thread),
		request = RunRequest(assistantId = assistant.await().id)
	).id

	override suspend fun fetchRun(thread: String, run: String) = openAI.getRun(
		threadId = ThreadId(thread),
		runId = RunId(run)
	).let {
		RunState(
			run,
			it.status,
			it.lastError?.let { error -> "Code: ${error.code}; Message: ${error.message}" })
	}

	override suspend fun fetchMessages(thread: String, olderMessage: String?, user: User) =
		openAI.messages(
			threadId = ThreadId(thread),
			before = olderMessage?.let { MessageId(it) }
		).flatMap { message ->
			message.content.map { content ->
				Message(
					messageId = message.id.id,
					creationDateTime = Instant.fromEpochSeconds(message.createdAt.toLong()),
					author = message.role.toAuthor(),
					user = user,
					messageContent = content.toAssistantMessage(),
					runId = message.runId?.id
				)
			}
		}

	private suspend fun initAssistant(): Assistant {
		val existingAssistant =
			openAI.assistants(limit = 5).firstOrNull { it.name == assistantName }
		if (existingAssistant == null || existingAssistant.tools.isEmpty()) {
			return openAI.assistant(
				request = AssistantRequest(
					name = assistantName,
					instructions = "You are a nutritional advisor specializing in healthy eating. You provide guidance on nutritious recipes," +
							"answer questions about healthy food choices, and help users maintain a balanced diet. Users can share links," +
							"which you will analyze and assess for healthiness, highlighting any potential issues and suggesting improvements where necessary." +
							"Please note, that you are not a doctor and do not provide medical advice",
					model = ModelId("gpt-4o"),
					tools = listOf(StoreRecipeFunction.tool())
				)
			)
		}
		return existingAssistant
	}

}