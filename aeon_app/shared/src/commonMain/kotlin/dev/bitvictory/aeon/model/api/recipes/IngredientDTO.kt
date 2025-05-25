package dev.bitvictory.aeon.model.api.recipes

import kotlinx.serialization.Serializable

@Serializable
data class IngredientDTO(
	val foodId: String,
	val foodName: String,
	val quantity: QuantityDTO,
	val note: String?
)

@Serializable
data class QuantityDTO(
	val value: Double,
	val unitOfMeasure: String?
)
