package dev.bitvictory.aeon.core.domain.entities.assistant.thread

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AeonThread(
	val id: String,
	val createdAt: Instant
)
