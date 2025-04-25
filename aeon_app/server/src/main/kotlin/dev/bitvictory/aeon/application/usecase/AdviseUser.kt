package dev.bitvictory.aeon.application.usecase

import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import kotlinx.datetime.Instant
import org.bson.types.ObjectId

interface AdviseUser {
    suspend fun startNewAdvisory(message: Message): Advisory

    suspend fun retrieveAdvisoryById(id: ObjectId): Advisory

    suspend fun retrieveMessages(id: ObjectId, lastTimestamp: Instant): List<Message>

    suspend fun addMessage(advisoryId: ObjectId, message: Message): Message
}