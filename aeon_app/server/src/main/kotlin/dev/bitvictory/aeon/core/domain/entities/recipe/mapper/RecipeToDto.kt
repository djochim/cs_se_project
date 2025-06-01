package dev.bitvictory.aeon.core.domain.entities.recipe.mapper

import dev.bitvictory.aeon.core.domain.entities.food.Translation
import dev.bitvictory.aeon.core.domain.entities.recipe.Ingredient
import dev.bitvictory.aeon.core.domain.entities.recipe.Quantity
import dev.bitvictory.aeon.core.domain.entities.recipe.Recipe
import dev.bitvictory.aeon.core.domain.entities.recipe.Step
import dev.bitvictory.aeon.core.domain.entities.recipe.Tip
import dev.bitvictory.aeon.model.api.TranslationDTO
import dev.bitvictory.aeon.model.api.recipes.IngredientDTO
import dev.bitvictory.aeon.model.api.recipes.QuantityDTO
import dev.bitvictory.aeon.model.api.recipes.RecipeDTO
import dev.bitvictory.aeon.model.common.dto.StepDTO
import dev.bitvictory.aeon.model.common.dto.TipDTO

fun Step.toDto() = StepDTO(this.number, this.description)

fun Tip.toDto() = TipDTO(this.description)

fun Quantity.toDto() = QuantityDTO(this.value, this.unitOfMeasure)

fun Translation.toDto() = TranslationDTO(this.language, this.text)

fun Ingredient.toDto() =
	IngredientDTO(this.foodId.toHexString(), this.canonicalFoodName, this.localization.map { it.toDto() }, this.quantity.toDto(), this.note)

fun Recipe.toDto(userReplace: String? = null) = RecipeDTO(
	this.id.toHexString(),
	this.name,
	userReplace ?: this.user.id,
	this.description,
	this.language,
	this.ingredients.map { it.toDto() },
	this.steps.map { it.toDto() },
	this.tips.map { it.toDto() }
)