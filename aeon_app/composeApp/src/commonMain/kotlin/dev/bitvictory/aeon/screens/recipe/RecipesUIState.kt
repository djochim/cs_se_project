package dev.bitvictory.aeon.screens.recipe

import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.api.recipes.RecipeHeaderDTO

data class RecipesUIState(
	val recipes: List<RecipeHeaderDTO> = listOf(),
	val searchQuery: String = "",
	val error: AeonError? = null
)
