package dev.bitvictory.aeon.core.domain.entities.advisory

import dev.bitvictory.aeon.core.domain.entities.user.User
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
	@Contextual val messages: List<Message>
)
