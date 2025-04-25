package dev.bitvictory.aeon.core.domain.entities.advisory

import kotlinx.serialization.Serializable

@Serializable
sealed class MessageContent

@Serializable
data class StringMessage(val content: String) : MessageContent()