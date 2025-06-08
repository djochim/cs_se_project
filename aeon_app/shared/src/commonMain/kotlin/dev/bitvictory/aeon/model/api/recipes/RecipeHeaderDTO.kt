package dev.bitvictory.aeon.model.api.recipes

import kotlinx.serialization.Serializable

@Serializable
data class RecipeHeaderDTO(
	val id: String,
	val name: String,
	val description: String,
	val language: String = "",
	val userId: String,
	val preparation: PreparationDTO = PreparationDTO(),
)
