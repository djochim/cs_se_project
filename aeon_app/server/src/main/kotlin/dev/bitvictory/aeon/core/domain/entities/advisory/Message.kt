package dev.bitvictory.aeon.core.domain.entities.advisory

import dev.bitvictory.aeon.core.domain.entities.assistant.Author
import dev.bitvictory.aeon.infrastructure.database.InstantAsBsonDateTime
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class Message @OptIn(ExperimentalUuidApi::class) constructor(
    @Serializable(with = InstantAsBsonDateTime::class) val creationDateTime: Instant,
    val author: Author,
    val messageContent: MessageContent,
    val runId: String? = null,
    val messageId: String? = null,
    val status: String? = null,
    val error: String? = null
)
