package dev.bitvictory.aeon.core.domain.entities.assistant.message

import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class AeonMessage(
	val id: String,
	val createdAt: Instant,
	val threadId: String = "",
	val runId: String? = null,
	val assistantId: String? = null,
	val role: Author,
	val status: AeonStatus,
	@Contextual val content: List<AeonMessageContent>
)

@Serializable
data class AeonMessageDelta(
	val role: Author,
	val content: List<AeonMessageContent>
)

@Serializable
sealed interface AeonMessageContent

@Serializable
data class TextMessageContent(
	val value: String
): AeonMessageContent

@Serializable
data class ImageMessageContent(
	val fileId: String
): AeonMessageContent

@Serializable
data class ImageURLMessageContent(
	val url: String
): AeonMessageContent