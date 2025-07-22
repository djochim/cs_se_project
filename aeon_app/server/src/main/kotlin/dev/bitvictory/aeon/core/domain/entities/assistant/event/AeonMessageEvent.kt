package dev.bitvictory.aeon.core.domain.entities.assistant.event

import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiMessage
import dev.bitvictory.aeon.core.domain.entities.assistant.message.AiMessageDelta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AeonMessageEvent(
    override val eventKey: String,
    @SerialName("event_message") open val message: AiMessage?
) : AeonAssistantEvent(eventKey) {
    @Serializable
    data class Created(override val message: AiMessage?) :
        AeonMessageEvent("thread.message.created", message)

    @Serializable
    data class InProgress(override val message: AiMessage?) :
        AeonMessageEvent("thread.message.in_progress", message)

    @Serializable
    data class Completed(override val message: AiMessage?) :
        AeonMessageEvent("thread.message.completed", message)

    @Serializable
    data class Incomplete(override val message: AiMessage?) :
        AeonMessageEvent("thread.message.incomplete", message)
}

@Serializable
data class AeonMessageDeltaEvent(val messageId: String?, val message: AiMessageDelta?) :
    AeonAssistantEvent("thread.message.delta")