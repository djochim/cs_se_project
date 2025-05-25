package dev.bitvictory.aeon.core.domain.usecases.food

import dev.bitvictory.aeon.core.domain.entities.food.Food
import dev.bitvictory.aeon.core.domain.entities.food.FoodScore

interface FoodPersistence {
	suspend fun insert(food: Food)
	suspend fun insertAll(foods: List<Food>)
	suspend fun searchAll(searchQuery: List<String>): Map<String, FoodScore?>
}