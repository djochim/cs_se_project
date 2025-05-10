package dev.bitvictory.aeon.core.domain.entities.recipe

import org.bson.types.ObjectId

data class Ingredient(
    val foodId: ObjectId,
    val foodName: String,
    val quantity: Quantity
)

data class Quantity(
    val value: Double,
    val unitOfMeasure: String
)
