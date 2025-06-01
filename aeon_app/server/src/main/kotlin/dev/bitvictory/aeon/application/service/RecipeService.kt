package dev.bitvictory.aeon.application.service

import dev.bitvictory.aeon.core.domain.entities.recipe.Recipe
import dev.bitvictory.aeon.infrastructure.database.repository.RecipeCollection
import dev.bitvictory.aeon.model.primitive.Page
import org.bson.types.ObjectId
import java.time.*
import java.util.*

val DEFAULT_INGREDIENT_ID: ObjectId = ObjectId.getSmallestWithDate(Date.from(Instant.EPOCH))

class RecipeService(
	private val recipeCollection: RecipeCollection,
	private val foodService: FoodService
) {

	suspend fun insert(recipe: Recipe) {
//        recipeCollection.insert(recipe.copy(ingredients = replacedIngredients))
	}

	suspend fun getAll(page: Page) = recipeCollection.getAll(page)

	suspend fun getById(id: ObjectId) = recipeCollection.getById(id)

	suspend fun delete(id: ObjectId) = recipeCollection.delete(id)

	suspend fun search(searchQuery: String, page: Page) = recipeCollection.search(searchQuery, page)

}