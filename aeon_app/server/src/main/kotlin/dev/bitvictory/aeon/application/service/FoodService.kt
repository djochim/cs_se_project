package dev.bitvictory.aeon.application.service

import dev.bitvictory.aeon.core.domain.entities.food.Food
import dev.bitvictory.aeon.infrastructure.database.repository.FoodCollection
import dev.bitvictory.aeon.model.primitive.Page
import org.bson.types.ObjectId

const val LIMIT_SCORE = 0.9

class FoodService(
	private val foodCollection: FoodCollection
) {

	suspend fun insert(food: Food) = foodCollection.insert(food)

	suspend fun getAll(page: Page) = foodCollection.getAll(page)

	suspend fun getById(id: ObjectId) = foodCollection.getById(id)

	suspend fun delete(id: ObjectId) = foodCollection.delete(id)

	suspend fun search(searchQuery: String, page: Page) = foodCollection.search(searchQuery, page)

	suspend fun findSimilar(searchQuery: String, page: Page) =
		foodCollection.search(searchQuery, page)

}