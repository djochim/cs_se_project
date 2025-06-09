package dev.bitvictory.aeon.application.usecases.recipes

import dev.bitvictory.aeon.core.domain.entities.recipe.Recipe
import dev.bitvictory.aeon.core.domain.entities.recipe.RecipeHeader
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.model.primitive.Page

interface ManageRecipes {
	suspend fun getAll(page: Page, user: User): List<RecipeHeader>
	suspend fun search(searchQuery: String, page: Page, user: User): List<RecipeHeader>
	suspend fun getById(id: String, user: User): Recipe
	suspend fun delete(id: String, user: User): Long
}