package dev.bitvictory.aeon.core.domain.usecases.recipe

import dev.bitvictory.aeon.core.domain.entities.recipe.Recipe

interface RecipePersistence {
	suspend fun insert(recipe: Recipe)
}