package dev.bitvictory.aeon.model.api.advisory

import dev.bitvictory.aeon.model.api.AuthorDTO
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class MessageDTO @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String? = Uuid.NIL.toHexString(),
    val messageContent: MessageContentDTO,
    val creationDateTime: Instant,
    val author: AuthorDTO,
    val status: String = "",
    val error: String = ""
)
