package dev.bitvictory.aeon.application.usecases.advise

import dev.bitvictory.aeon.core.domain.entities.advisory.Advisory
import dev.bitvictory.aeon.core.domain.entities.advisory.Message
import dev.bitvictory.aeon.core.domain.entities.user.User
import kotlinx.datetime.Instant
import org.bson.types.ObjectId

interface AdviseUser {
	suspend fun startNewAdvisory(message: Message): Advisory

	suspend fun retrieveAdvisoryById(id: ObjectId, user: User): Advisory

	suspend fun retrieveMessages(id: ObjectId, lastTimestamp: Instant): List<Message>

	suspend fun addMessage(advisoryId: ObjectId, message: Message): Message
}