package dev.bitvictory.aeon.screens.recipe.detail

import dev.bitvictory.aeon.model.api.recipes.RecipeDTO
import dev.bitvictory.aeon.model.api.recipes.RecipeHeaderDTO

data class RecipeDetailUIState(
	val recipeHeader: RecipeHeaderDTO,
	val recipe: RecipeDTO? = null,
)
