package dev.bitvictory.aeon.model.api.advisory

import kotlinx.serialization.Serializable

@Serializable
data class AdvisoryDTO(
    val id: String,
    val threadId: String,
    val messages: List<MessageDTO>
)
