package dev.bitvictory.aeon.model.api.recipes

import dev.bitvictory.aeon.model.api.TranslationDTO
import kotlinx.serialization.Serializable

@Serializable
data class IngredientDTO(
	val foodId: String,
	val canonicalFoodName: String,
	val translations: List<TranslationDTO?>,
	val quantity: QuantityDTO,
	val note: String?
)

@Serializable
data class QuantityDTO(
	val value: Double,
	val unitOfMeasure: String?
)
