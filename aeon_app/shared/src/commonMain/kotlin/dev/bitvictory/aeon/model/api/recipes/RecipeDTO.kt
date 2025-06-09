package dev.bitvictory.aeon.model.api.recipes

import dev.bitvictory.aeon.model.common.dto.StepDTO
import dev.bitvictory.aeon.model.common.dto.TipDTO
import kotlinx.serialization.Serializable

@Serializable
data class RecipeDTO(
	val id: String,
	val name: String,
	val description: String,
	val language: String,
	val userId: String,
	val ingredients: List<IngredientDTO>,
	val steps: List<StepDTO>,
	val tips: List<TipDTO>,
	val preparation: PreparationDTO = PreparationDTO()
)
