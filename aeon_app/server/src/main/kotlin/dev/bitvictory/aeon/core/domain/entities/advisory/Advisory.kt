package dev.bitvictory.aeon.core.domain.entities.advisory

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class Advisory(
    @SerialName("_id")
    @Contextual val id: ObjectId,
    val threadId: String,
    @Contextual val messages: List<Message>
)
