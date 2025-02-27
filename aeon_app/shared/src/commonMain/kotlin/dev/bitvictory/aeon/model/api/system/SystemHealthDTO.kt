package dev.bitvictory.aeon.model.api.system

import dev.bitvictory.aeon.model.primitive.UptimeStatus
import kotlinx.serialization.Serializable

@Serializable
data class SystemHealthDTO(val status: UptimeStatus, val subcomponents: List<SystemComponentHealthDTO>)