package dev.bitvictory.aeon.application.service

import dev.bitvictory.aeon.application.service.actioncall.ActionCallDispatcher
import dev.bitvictory.aeon.application.service.eventhandling.AdvisoryEventDispatcher
import dev.bitvictory.aeon.application.usecases.advise.AdviseUser
import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.advisory.StringMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.HealthAssistant
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonAssistantEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AeonMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.message.Author
import dev.bitvictory.aeon.core.domain.entities.assistant.message.TextMessageContent
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import dev.bitvictory.aeon.core.domain.usecases.assistant.AsyncAssistantExecution
import dev.bitvictory.aeon.core.exceptions.InvalidMessageException
import dev.bitvictory.aeon.exceptions.AuthorizationException
import dev.bitvictory.aeon.infrastructure.network.dto.AssistantMessageDTO
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.bson.types.ObjectId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AdvisoryService(
	private val advisoryPersistence: AdvisoryPersistence,
	private val asyncAssistantExecution: AsyncAssistantExecution,
	private val advisoryEventDispatcher: AdvisoryEventDispatcher,
	private val actionCallDispatcher: ActionCallDispatcher,
	private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
): AdviseUser {

	private val logger = KtorSimpleLogger(this.javaClass.name)

	/**
	 * Starts a new advisory based on the provided message.
	 *
	 * This function performs the following steps:
	 * 1. Extracts the string content from the input `Message`.
	 * 2. Creates a new `Advisory` object with an initial `AeonMessage` based on the input.
	 *    - The advisory is initialized with a status of `CREATED`.
	 *    - The initial message is from the `USER` and has a status of `FINALIZED`.
	 * 3. Persists the newly created `Advisory` using `advisoryPersistence`.
	 * 4. Launches a background coroutine to handle the event flow.
	 *    - This involves executing an assistant (specifically `HealthAssistant`) with the message content.
	 *    - The `handleEventFlow` function is called to process the assistant's response.
	 * 5. Returns the newly created `Advisory` object.
	 *
	 * @param message The initial message from the user that triggers the new advisory.
	 * @return The newly created `Advisory` object.
	 * @throws IllegalArgumentException if the `message.messageContent` is not a `StringMessage`.
	 *                                  (Implicitly, as the `when` expression is not exhaustive,
	 *                                  though in this specific implementation it only handles `StringMessage`.)
	 */
	@OptIn(ExperimentalUuidApi::class)
	override suspend fun startNewAdvisory(message: Message): Advisory {
		val stringMessage = when (val content = message.messageContent) {
			is StringMessage -> content.content
		}
		val advisory = Advisory(
			ObjectId.get(), "", message.user, AeonStatus.CREATED, listOf(), listOf(), listOf(
				AeonMessage(
					id = Uuid.random().toHexString(),
					createdAt = message.creationDateTime,
					role = Author.USER,
					status = AeonStatus.FINALIZED,
					content = listOf(TextMessageContent(stringMessage)),
				)
			)
		)
		advisoryPersistence.insert(advisory)
		CoroutineScope(backgroundDispatcher).launch {
			handleEventFlow(
				advisory.id, message.user, asyncAssistantExecution.executeThread(
					HealthAssistant, AssistantMessageDTO(
						message.author,
						stringMessage
					)
				)
			)
		}
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

	/**
	 * Adds a new message to an existing advisory.
	 *
	 * The message content must be a String. It validates that the user adding the message
	 * is the same user associated with the advisory's thread context.
	 *
	 * The message is then sent to an assistant (HealthAssistant in this case) for processing
	 * in a background coroutine. Simultaneously, the message is persisted to the advisory's
	 * message history.
	 *
	 * @param advisoryId The ObjectId of the advisory to which the message will be added.
	 * @param message The Message object containing the content, author, user, and creation timestamp.
	 * @return The original Message object that was added.
	 * @throws InvalidMessageException if the message content is not a String.
	 * @throws AuthorizationException if the user attempting to add the message is not authorized
	 *                                for the advisory's thread.
	 */
	@OptIn(ExperimentalUuidApi::class)
	override suspend fun addMessage(advisoryId: ObjectId, message: Message): Message {
		val stringMessage = when (val content = message.messageContent) {
			is StringMessage -> content.content
			else             -> throw InvalidMessageException("Message content of type ${content.javaClass.kotlin}")
		}
		val threadContext = advisoryPersistence.getThreadContext(advisoryId)
		if (threadContext.user != message.user) {
			throw AuthorizationException("User is not allowed to add messages to this advisory")
		}

		CoroutineScope(backgroundDispatcher).launch {
			handleEventFlow(
				advisoryId, message.user, asyncAssistantExecution.writeMessageToThread(
					HealthAssistant, threadContext.threadId, AssistantMessageDTO(
						message.author,
						stringMessage
					)
				)
			)
		}
		advisoryPersistence.appendMessages(
			advisoryId, listOf(
				AeonMessage(
					id = Uuid.random().toHexString(),
					createdAt = message.creationDateTime,
					role = Author.USER,
					status = AeonStatus.FINALIZED,
					content = listOf(TextMessageContent(stringMessage)),
				)
			)
		)
		return message
	}

	/**
	 * Handles the flow of Aeon Assistant events.
	 *
	 * This function processes a stream of events, dispatches them, executes associated actions,
	 * submits results, and recursively handles any further event flows generated.
	 * It includes error handling at each stage of the processing pipeline.
	 *
	 * @param advisoryId The ObjectId of the advisory associated with these events.
	 * @param user The User initiating or involved in the event flow.
	 * @param eventFlow A Flow of [AeonAssistantEvent] to be processed.
	 */
	private suspend fun handleEventFlow(advisoryId: ObjectId, user: User, eventFlow: Flow<AeonAssistantEvent>) {
		eventFlow.catch { e -> logger.error("Error executing event flow", e) }
			.map { advisoryEventDispatcher.dispatch(advisoryId = advisoryId, it) }
			.catch { e -> logger.error("Error executing thread", e) }
			.filter { it.requiresDispatching() }
			.map { actionCallDispatcher.dispatch(advisoryId, user, it) }
			.catch { e -> logger.error("Error executing function call", e) }
			.map { asyncAssistantExecution.submitActionResult(it.threadId, it.runId, it.outputs) }
			.catch { e -> logger.error("Error submitting function call", e) }
			.collect {
				try {
					handleEventFlow(advisoryId, user, it)
				} catch (e: Exception) {
					logger.error("Error handling event flow", e)
				}
			}
	}

}