package dev.bitvictory.aeon.core.domain.entities.recipe

import dev.bitvictory.aeon.core.domain.entities.food.Translation
import org.bson.types.ObjectId

data class Ingredient(
	val foodId: ObjectId,
	val canonicalFoodName: String,
	val quantity: Quantity,
	val note: String?,
	val localization: List<Translation>
)

data class Quantity(
	val value: Double,
	val canonicalUnitOfMeasure: String?,
	val unitOfMeasure: String?
)
