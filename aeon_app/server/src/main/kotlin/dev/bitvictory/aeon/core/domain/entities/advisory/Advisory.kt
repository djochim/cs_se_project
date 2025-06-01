package dev.bitvictory.aeon.core.domain.entities.advisory

import dev.bitvictory.aeon.core.domain.entities.assistant.event.AeonAssistantEvent
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AeonMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.model.AeonError
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class Advisory(
	@SerialName("_id")
	@Contextual val id: ObjectId,
	val threadId: String,
	val user: User,
	val status: AeonStatus,
	@Contextual val errors: List<AeonError> = emptyList(),
	@Contextual val events: List<AeonAssistantEvent> = emptyList(),
	@Contextual val messages: List<AeonMessage> = emptyList()
)
