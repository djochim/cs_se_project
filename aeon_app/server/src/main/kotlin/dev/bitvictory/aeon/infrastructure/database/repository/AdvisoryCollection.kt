package dev.bitvictory.aeon.infrastructure.database.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.AdvisoryMessages
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.advisory.ThreadContext
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonAssistantEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonMessageEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonThreadEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.ErrorEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AeonMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import dev.bitvictory.aeon.infrastructure.database.Database
import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.primitive.Page
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant
import org.bson.types.ObjectId

class AdvisoryCollection(database: Database): AdvisoryPersistence {

	companion object {
		const val COLLECTION_NAME = "advisories"
	}

	private val collection = database.value.getCollection<Advisory>(COLLECTION_NAME)
	private val client = database.client
	private val logger = KtorSimpleLogger(this.javaClass.name)

	override suspend fun insert(advisory: Advisory) {
		logger.debug("Insert advisory {}", advisory.id)
		collection.insertOne(advisory)
	}

	override suspend fun appendMessages(advisoryId: ObjectId, messages: List<AeonMessage>) {
		logger.debug("Write message to advisory {}", advisoryId)
		val filter = Filters.eq("_id", advisoryId)
		val update = Updates.pushEach(Advisory::messages.name, messages)
		collection.updateOne(filter, update)
	}

	override suspend fun updateRunStatus(
		advisoryId: ObjectId,
		runId: String,
		status: String,
		error: String?
	) {
		val filter = Filters.and(
			Filters.eq("_id", advisoryId),
			Filters.elemMatch(
				"messages",
				Filters.eq("runId", runId)
			)
		)
		val update =
			Updates.combine(
				Updates.set(
					"messages.$.status",
					status
				),
				Updates.set(
					"messages.$.error",
					error
				)
			)
		collection.updateOne(filter, update)
	}

	override suspend fun setMessages(advisoryId: ObjectId, messages: List<Message>) {
		logger.debug("Set message of advisory {}", advisoryId)
		val filter = Filters.eq("_id", advisoryId)
		val update = Updates.set(Advisory::messages.name, messages)
		collection.updateOne(filter, update)
	}

	override suspend fun getAll(page: Page): List<Advisory> {
		logger.debug("Get Advisory page {}", page)
		return collection.find<Advisory>().sort(Sorts.ascending(Advisory::id.name))
			.limit(page.limit)
			.skip(page.offset).toList()
	}

	override suspend fun getNewMessages(id: ObjectId, lastTimestamp: Instant): List<Message> {
		logger.debug("Get new Advisory messages of advisory {}", id)
		val filter = Filters.eq("_id", id)
		val projection = Projections.fields(
			Projections.include(Advisory::messages.name),
			Projections.excludeId()
		)
		return collection.find<AdvisoryMessages>(filter).projection(projection)
			.first().messages.filter { it.creationDateTime > lastTimestamp }
	}

	override suspend fun getThreadContext(id: ObjectId): ThreadContext {
		logger.debug("Get Thread id of {}", id)
		val filter = Filters.eq("_id", id)
		val projection = Projections.fields(
			Projections.include(Advisory::threadId.name, Advisory::user.name),
			Projections.excludeId()
		)
		return collection.find<ThreadContext>(filter).projection(projection).first()
	}

	override suspend fun getById(id: ObjectId): Advisory {
		logger.debug("Get Advisory by id {}", id)
		val filter = Filters.eq("_id", id)
		// Do not load events, as they are only used for debugging and can be very large
		val projection = Projections.fields(
			Projections.exclude(Advisory::events.name)
		)
		return collection.find<Advisory>(filter).projection(projection).first()
	}

	override suspend fun delete(id: ObjectId) {
		logger.debug("Delete Advisory by id {}", id)
		val filter = Filters.eq("_id", id)
		collection.deleteOne(filter)
	}

	override suspend fun createThread(advisoryId: ObjectId, event: AeonThreadEvent.Created) {
		val filter = Filters.eq("_id", advisoryId)
		val updates = mutableListOf(Updates.addToSet(Advisory::events.name, event), Updates.set(Advisory::status.name, AeonStatus.PENDING))
		event.thread?.also { updates.add(Updates.set(Advisory::threadId.name, it.id)) }
		collection.updateOne(filter, Updates.combine(updates))
	}

	override suspend fun addEvent(advisoryId: ObjectId, event: AeonAssistantEvent) {
		val filter = Filters.eq("_id", advisoryId)
		val update = Updates.push(Advisory::events.name, event)
		collection.updateOne(filter, update)
	}

	override suspend fun logErrorEvent(advisoryId: ObjectId, event: ErrorEvent) {
		val filter = Filters.eq("_id", advisoryId)
		val updates = mutableListOf(Updates.addToSet(Advisory::events.name, event), Updates.set(Advisory::status.name, AeonStatus.FAILED_OR_CANCELLED))
		event.data?.also { updates.add(Updates.addToSet(Advisory::errors.name, AeonError(message = it))) }
		collection.updateOne(filter, Updates.combine(updates))
	}

	override suspend fun updateStatus(advisoryId: ObjectId, status: AeonStatus, event: AeonAssistantEvent) {
		val filter = Filters.eq("_id", advisoryId)
		val updates = mutableListOf(Updates.addToSet(Advisory::events.name, event), Updates.set(Advisory::status.name, status))
		collection.updateOne(filter, Updates.combine(updates))
	}

	override suspend fun upsertMessage(advisoryId: ObjectId, message: AeonMessage?, event: AeonMessageEvent) {
		if (message == null) {
			addEvent(advisoryId, event)
			return
		}

		val pullFilter = Filters.eq("_id", advisoryId)
		val pullUpdate = Updates.pull(
			Advisory::messages.name,
			Filters.eq(
				AeonMessage::id.name,
				message.id
			)
		)
		val pullResult = collection.updateOne(pullFilter, pullUpdate)
		if (!pullResult.wasAcknowledged()) {
			logger.warn("Pull operation for message {} in advisory {} was not acknowledged.", message.id, advisoryId)
			// Consider error handling or retrying
		}

		val filter = Filters.eq("_id", advisoryId)

		val updates = mutableListOf(Updates.push(Advisory::events.name, event), Updates.push(Advisory::messages.name, message))
		collection.updateOne(filter, Updates.combine(updates))
	}
}