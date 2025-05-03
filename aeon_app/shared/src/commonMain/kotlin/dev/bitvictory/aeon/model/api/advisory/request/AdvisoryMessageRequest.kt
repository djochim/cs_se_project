package dev.bitvictory.aeon.model.api.advisory.request

import dev.bitvictory.aeon.model.api.advisory.MessageContentDTO
import kotlinx.serialization.Serializable

@Serializable
data class AdvisoryMessageRequest(
    val message: MessageContentDTO
)
