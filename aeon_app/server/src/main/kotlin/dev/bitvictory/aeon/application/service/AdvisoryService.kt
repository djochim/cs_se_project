package dev.bitvictory.aeon.application.service

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.core.Status
import dev.bitvictory.aeon.application.usecases.advise.AdviseUser
import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.advisory.StringMessage
import dev.bitvictory.aeon.core.domain.entities.advisory.ThreadContext
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import dev.bitvictory.aeon.core.domain.usecases.assistant.AssistantExecution
import dev.bitvictory.aeon.core.exceptions.InvalidMessageException
import dev.bitvictory.aeon.exceptions.AuthorizationException
import dev.bitvictory.aeon.infrastructure.network.dto.AssistantMessageDTO
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.bson.types.ObjectId
import kotlin.time.Duration.Companion.seconds

@OptIn(BetaOpenAI::class)
class AdvisoryService(
	private val advisoryPersistence: AdvisoryPersistence,
	private val assistantExecution: AssistantExecution,
	private val waitForCompletion: Boolean = true
): AdviseUser {

	private val logger = KtorSimpleLogger(this.javaClass.name)

	override suspend fun startNewAdvisory(message: Message): Advisory {
		val stringMessage = when (val content = message.messageContent) {
			is StringMessage -> content.content
		}
		val threadId =
			assistantExecution.initiateThread(
				AssistantMessageDTO(
					message.author,
					stringMessage
				)
			).id
		val openAIMessage =
			assistantExecution.fetchMessages(threadId, null, message.user)
				.minByOrNull { message.creationDateTime }
		val runId = assistantExecution.executeAssistant(threadId).id
		val advisory = Advisory(ObjectId.get(), threadId, message.user, listOf((openAIMessage ?: message).copy(runId = runId)))
		advisoryPersistence.insert(advisory)
		waitForCompletion(advisory.id, ThreadContext(threadId, message.user), runId, openAIMessage?.messageId)
		return advisory
	}

	override suspend fun retrieveAdvisoryById(id: ObjectId, user: User): Advisory {
		val advisory = advisoryPersistence.getById(id)
		if (advisory.user != user) {
			throw AuthorizationException("User is not allowed to access this advisory")
		}
		return advisory
	}

	override suspend fun retrieveMessages(id: ObjectId, lastTimestamp: Instant): List<Message> {
		return advisoryPersistence.getNewMessages(id, lastTimestamp)
	}

	override suspend fun addMessage(advisoryId: ObjectId, message: Message): Message {
		val stringMessage = when (val content = message.messageContent) {
			is StringMessage -> content.content
			else             -> throw InvalidMessageException("Message content of type ${content.javaClass.kotlin}")
		}
		val threadContext = advisoryPersistence.getThreadContext(advisoryId)
		if (threadContext.user != message.user) {
			throw AuthorizationException("User is not allowed to add messages to this advisory")
		}
		val messageId = assistantExecution.writeMessageToThread(
			threadContext.threadId, AssistantMessageDTO(
				message.author,
				stringMessage
			)
		).id
		val runId = assistantExecution.executeAssistant(threadContext.threadId).id
		val executedMessage = message.copy(runId = runId, messageId = messageId)
		advisoryPersistence.appendMessages(advisoryId, listOf(executedMessage))
		waitForCompletion(advisoryId, threadContext, runId, lastMessage = messageId)
		return executedMessage
	}

	private fun waitForCompletion(
		advisoryId: ObjectId,
		threadContext: ThreadContext,
		runId: String,
		lastMessage: String?
	) {
		if (!waitForCompletion) {
			return
		}
		CoroutineScope(Dispatchers.IO).launch {
			do {
				delay(1.seconds)
				val retrievedRun = assistantExecution.fetchRun(threadContext.threadId, runId)
				advisoryPersistence.updateRunStatus(
					advisoryId,
					runId,
					retrievedRun.status.value,
					retrievedRun.error
				)
				logger.debug("Status is ${retrievedRun.status}")
			} while (retrievedRun.status != Status.Completed)

			val messages = assistantExecution.fetchMessages(threadContext.threadId, lastMessage, threadContext.user)
			if (lastMessage == null) {
				advisoryPersistence.setMessages(advisoryId, messages)
			} else {
				advisoryPersistence.appendMessages(advisoryId, messages)
			}
		}
	}

}