package dev.bitvictory.aeon.core.domain.usecases.recipe

import dev.bitvictory.aeon.core.domain.entities.recipe.Recipe

interface CreateRecipeUseCase {
    fun create(recipe: Recipe): Recipe
}