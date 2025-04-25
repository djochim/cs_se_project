package dev.bitvictory.aeon.core.domain.usecases.advisory

import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.model.primitive.Page
import kotlinx.datetime.Instant
import org.bson.types.ObjectId

interface AdvisoryPersistence {
    suspend fun getById(queryId: ObjectId): Advisory
    suspend fun getAll(page: Page): List<Advisory>
    suspend fun getThreadId(id: ObjectId): String
    suspend fun getNewMessages(id: ObjectId, lastTimestamp: Instant): List<Message>

    suspend fun insert(advisory: Advisory)
    suspend fun appendMessages(advisoryId: ObjectId, messages: List<Message>)
    suspend fun updateRunStatus(advisoryId: ObjectId, runId: String, status: String, error: String?)
    suspend fun setMessages(advisoryId: ObjectId, messages: List<Message>)
    suspend fun delete(id: ObjectId)
}