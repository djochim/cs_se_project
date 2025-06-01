package dev.bitvictory.aeon.core.domain.entities.assistant.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AeonAssistantEvent(
	@SerialName("event_key")
	open val eventKey: String
)

@Serializable
data class UnknownEvent(val data: String?): AeonAssistantEvent("unknown")

@Serializable
data class ErrorEvent(val data: String?): AeonAssistantEvent("error")

@Serializable
data class DoneEvent(val data: String?): AeonAssistantEvent("done")