package dev.bitvictory.aeon.core.domain.usecases.recipe

import dev.bitvictory.aeon.core.domain.entities.recipe.Recipe
import dev.bitvictory.aeon.core.domain.entities.recipe.RecipeHeader
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.model.primitive.Page
import org.bson.types.ObjectId

interface RecipePersistence {
	suspend fun insert(recipe: Recipe)
	suspend fun getAll(page: Page, user: User): List<RecipeHeader>
	suspend fun search(searchQuery: String, page: Page, user: User): List<RecipeHeader>
	suspend fun getById(id: ObjectId, user: User): Recipe
	suspend fun delete(id: ObjectId, user: User): Long
}