package dev.bitvictory.aeon.model.api.recipes

import kotlinx.serialization.Serializable

@Serializable
data class RecipesDTO(
    val items: List<RecipeDTO>
)
