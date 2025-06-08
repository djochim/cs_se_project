package dev.bitvictory.aeon.service

import dev.bitvictory.aeon.client.aeon.AeonApi
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.api.recipes.RecipeDTO
import dev.bitvictory.aeon.model.api.recipes.RecipesDTO
import dev.bitvictory.aeon.model.primitive.Page

interface IRecipeService {
	suspend fun getRecipes(page: Page, searchQuery: String?): AeonResponse<RecipesDTO>
	suspend fun getRecipe(id: String): AeonResponse<RecipeDTO>
}

class RecipeService(
	private val aeonApi: AeonApi,
): IRecipeService {

	override suspend fun getRecipes(page: Page, searchQuery: String?): AeonResponse<RecipesDTO> = aeonApi.getRecipes(page, searchQuery)

	override suspend fun getRecipe(id: String): AeonResponse<RecipeDTO> = aeonApi.getRecipe(id)

}