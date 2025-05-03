package dev.bitvictory.aeon.infrastructure.database.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.AdvisoryMessages
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.advisory.ThreadContext
import dev.bitvictory.aeon.core.domain.usecases.advisory.AdvisoryPersistence
import dev.bitvictory.aeon.infrastructure.database.Database
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
	private val logger = KtorSimpleLogger(this.javaClass.name)

	override suspend fun insert(advisory: Advisory) {
		logger.debug("Insert advisory {}", advisory.id)
		collection.insertOne(advisory)
	}

	override suspend fun appendMessages(advisoryId: ObjectId, messages: List<Message>) {
		logger.debug("Write message to advisory {}", advisoryId)
		val filter = Filters.eq("_id", advisoryId)
		val update = Updates.addEachToSet(Advisory::messages.name, messages)
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
		return collection.find<Advisory>(filter).first()
	}

	override suspend fun delete(id: ObjectId) {
		logger.debug("Delete Advisory by id {}", id)
		val filter = Filters.eq("_id", id)
		collection.deleteOne(filter)
	}

}