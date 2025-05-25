package dev.bitvictory.aeon.core.domain.entities.assistant.event

import dev.bitvictory.aeon.core.domain.entities.assistant.thread.AeonThread
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AeonThreadEvent(override val eventKey: String, @SerialName("event_thread") open val thread: AeonThread?): AeonAssistantEvent(eventKey) {
	@Serializable
	data class Created(override val thread: AeonThread?): AeonThreadEvent("thread.created", thread)
}