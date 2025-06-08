package dev.bitvictory.aeon.application.service

import dev.bitvictory.aeon.application.usecases.recipes.ManageRecipes
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.core.domain.usecases.recipe.RecipePersistence
import dev.bitvictory.aeon.model.primitive.Page
import org.bson.types.ObjectId

class RecipeService(
	private val recipePersistence: RecipePersistence
): ManageRecipes {

	override suspend fun getAll(page: Page, user: User) = recipePersistence.getAll(page, user)

	override suspend fun search(searchQuery: String, page: Page, user: User) = recipePersistence.search(searchQuery, page, user)

	override suspend fun getById(id: String, user: User) = recipePersistence.getById(ObjectId(id), user)

	override suspend fun delete(id: String, user: User) = recipePersistence.delete(ObjectId(id), user)

}