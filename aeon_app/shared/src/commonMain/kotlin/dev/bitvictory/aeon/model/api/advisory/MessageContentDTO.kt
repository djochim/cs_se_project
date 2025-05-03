package dev.bitvictory.aeon.model.api.advisory

import kotlinx.serialization.Serializable

@Serializable
sealed class MessageContentDTO

@Serializable
data class StringMessageDTO(val content: String) : MessageContentDTO()
