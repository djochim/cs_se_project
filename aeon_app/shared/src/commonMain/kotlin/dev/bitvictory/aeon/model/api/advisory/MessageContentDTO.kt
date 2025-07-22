package dev.bitvictory.aeon.model.api.advisory

import kotlinx.serialization.Serializable

@Serializable
sealed class MessageContentDTO {
    abstract fun isValid(): Boolean
}

@Serializable
data class StringMessageDTO(val content: String) : MessageContentDTO() {
    override fun isValid(): Boolean {
        return content.isNotBlank()
    }
}

@Serializable
data class ImageMessageDTO(val content: ByteArray) : MessageContentDTO() {
    override fun isValid(): Boolean {
        return content.isNotEmpty()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ImageMessageDTO

        return content.contentEquals(other.content)
    }

    override fun hashCode(): Int {
        return content.contentHashCode()
    }
}
