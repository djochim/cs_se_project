package dev.bitvictory.aeon.core.domain.entities.assistant.message

import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonStatus
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class AiMessage(
    val id: String,
    val createdAt: Instant,
    val threadId: String = "",
    val runId: String? = null,
    val assistantId: String? = null,
    val role: Author,
    val status: AeonStatus,
    @Contextual val content: List<AiMessageContent>
)

@Serializable
data class AiMessageDelta(
    val role: Author,
    val content: List<AiMessageContent>
)

@Serializable
sealed interface AiMessageContent

@Serializable
data class AiTextMessageContent(
    val value: String
) : AiMessageContent


@Serializable
data class AiImageMessageContent(
    val content: ByteArray
) : AiMessageContent {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AiImageMessageContent

        return content.contentEquals(other.content)
    }

    override fun hashCode(): Int {
        return content.contentHashCode()
    }
}

@Serializable
data class AiImageIdMessageContent(
    val fileId: String
) : AiMessageContent

@Serializable
data class AiImageURLMessageContent(
    val url: String
) : AiMessageContent