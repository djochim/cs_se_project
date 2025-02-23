package dev.bitvictory.aeon.core.domain.entities.system

import dev.bitvictory.aeon.model.primitive.UptimeStatus

/**
 * Health status of a single system component
 */
data class SystemComponentHealth(
	val name: String,
	val status: UptimeStatus,
	val message: String = ""
)
