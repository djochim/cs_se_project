package dev.bitvictory.aeon.core.domain.usecases.food

import dev.bitvictory.aeon.core.domain.entities.food.Food
import dev.bitvictory.aeon.core.domain.entities.food.FoodScore
import dev.bitvictory.aeon.core.domain.entities.food.Translation
import org.bson.types.ObjectId

interface FoodPersistence {
	suspend fun insert(food: Food)
	suspend fun insertAll(foods: List<Food>)
	suspend fun searchAll(searchQuery: List<String>, language: String): Map<String, FoodScore?>
	suspend fun addTranslations(missingTranslations: Map<ObjectId, List<Translation>>)
}