package dev.bitvictory.aeon.core.domain.usecases.advisory

import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.advisory.ThreadContext
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonAssistantEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonMessageEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonThreadEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.event.ErrorEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AeonMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus
import dev.bitvictory.aeon.model.primitive.Page
import kotlinx.datetime.Instant
import org.bson.types.ObjectId

interface AdvisoryPersistence {
	suspend fun getById(queryId: ObjectId): Advisory
	suspend fun getAll(page: Page): List<Advisory>
	suspend fun getThreadContext(id: ObjectId): ThreadContext
	suspend fun getNewMessages(id: ObjectId, lastTimestamp: Instant): List<Message>

	suspend fun insert(advisory: Advisory)
	suspend fun appendMessages(advisoryId: ObjectId, messages: List<AeonMessage>)
	suspend fun updateRunStatus(advisoryId: ObjectId, runId: String, status: String, error: String?)
	suspend fun setMessages(advisoryId: ObjectId, messages: List<Message>)
	suspend fun delete(id: ObjectId)
	suspend fun createThread(advisoryId: ObjectId, event: AeonThreadEvent.Created)
	suspend fun addEvent(advisoryId: ObjectId, event: AeonAssistantEvent)
	suspend fun logErrorEvent(advisoryId: ObjectId, event: ErrorEvent)
	suspend fun updateStatus(advisoryId: ObjectId, status: AeonStatus, event: AeonAssistantEvent)
	suspend fun upsertMessage(advisoryId: ObjectId, message: AeonMessage?, event: AeonMessageEvent)
}