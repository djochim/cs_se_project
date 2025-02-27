package dev.bitvictory.aeon.model.api.system

import dev.bitvictory.aeon.model.primitive.UptimeStatus
import kotlinx.serialization.Serializable

@Serializable
data class SystemComponentHealthDTO(val name: String, val status: UptimeStatus, val message: String = "")