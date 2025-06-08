package dev.bitvictory.aeon.model.api.recipes

import kotlinx.serialization.Serializable

@Serializable
data class PreparationDTO(
	val durationMinutes: Int = 0,
	val complexity: Int = 0
)