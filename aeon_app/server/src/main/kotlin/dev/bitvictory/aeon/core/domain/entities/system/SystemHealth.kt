package dev.bitvictory.aeon.core.domain.entities.system

import dev.bitvictory.aeon.model.primitive.UptimeStatus

data class SystemHealth(
	val components: List<SystemComponentHealth>
) {
	val status: UptimeStatus
		get() = components.firstOrNull { it.status == UptimeStatus.DOWN }?.status ?: UptimeStatus.UP
}
