package dev.bitvictory.aeon.core.domain.entities.recipe.fio

import kotlinx.serialization.Serializable

@Serializable
data class RecipeFIO(
	val id: String? = null,
	val name: String,
	val language: String,
	val description: String,
	val ingredients: List<IngredientFIO>,
	val steps: List<StepFIO>,
	val tips: List<String>?
)
