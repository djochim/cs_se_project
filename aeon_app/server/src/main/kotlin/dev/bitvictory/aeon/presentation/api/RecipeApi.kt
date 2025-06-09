package dev.bitvictory.aeon.presentation.api

import dev.bitvictory.aeon.application.service.RecipeService
import dev.bitvictory.aeon.configuration.userPrincipal
import dev.bitvictory.aeon.core.domain.entities.food.Translation
import dev.bitvictory.aeon.core.domain.entities.recipe.Ingredient
import dev.bitvictory.aeon.core.domain.entities.recipe.PreparationDetails
import dev.bitvictory.aeon.core.domain.entities.recipe.Quantity
import dev.bitvictory.aeon.core.domain.entities.recipe.Recipe
import dev.bitvictory.aeon.core.domain.entities.recipe.RecipeHeader
import dev.bitvictory.aeon.core.domain.entities.recipe.Step
import dev.bitvictory.aeon.core.domain.entities.recipe.Tip
import dev.bitvictory.aeon.model.api.TranslationDTO
import dev.bitvictory.aeon.model.api.recipes.IngredientDTO
import dev.bitvictory.aeon.model.api.recipes.PreparationDTO
import dev.bitvictory.aeon.model.api.recipes.QuantityDTO
import dev.bitvictory.aeon.model.api.recipes.RecipeDTO
import dev.bitvictory.aeon.model.api.recipes.RecipeHeaderDTO
import dev.bitvictory.aeon.model.api.recipes.RecipesDTO
import dev.bitvictory.aeon.model.common.dto.StepDTO
import dev.bitvictory.aeon.model.common.dto.TipDTO
import dev.bitvictory.aeon.model.primitive.Page
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

/**
 * Defines the routes for recipe-related operations.
 *
 * All routes under "/recipes" require authentication.
 */
fun Route.recipes() {
	authenticate {
		route("/recipes") {
			val recipeService by inject<RecipeService>()
			get {
				val offset = call.request.queryParameters["offset"]?.toInt() ?: 0
				val limit = call.request.queryParameters["limit"]?.toInt() ?: 50
				val searchQuery = call.request.queryParameters["search"]

				val recipes = if (searchQuery != null) {
					recipeService.search(searchQuery, Page(offset, limit), call.userPrincipal())
				} else {
					recipeService.getAll(Page(offset, limit), call.userPrincipal())
				}
				call.respond(HttpStatusCode.OK, recipes.toDto())
			}
			get("/{id}") {
				val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing request id")
				val recipe = recipeService.getById(id, call.userPrincipal())
				call.respond(HttpStatusCode.OK, recipe.toDto())
			}
			delete("/{id}") {
				val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing request id")
				recipeService.delete(id, call.userPrincipal())
				call.respond(HttpStatusCode.NoContent)
			}
		}
	}
}

fun PreparationDetails.toDto() =
	PreparationDTO(this.durationMinutes ?: 0, this.complexity ?: 0)

fun mapper(tip: TipDTO): Tip {
	return Tip(tip.description)
}

fun Tip.toDto() = TipDTO(this.description)

fun List<RecipeHeader>.toDto() = RecipesDTO(this.map { it.toDto() })

fun RecipeHeader.toDto() = RecipeHeaderDTO(
	this.id.toHexString(),
	this.name,
	this.description,
	this.language,
	this.user.id,
	this.preparationDetails?.toDto() ?: PreparationDTO()
)

fun Recipe.toDto(): RecipeDTO {
	val steps = this.steps.map { it.toDto() }
	val ingredients = this.ingredients.map { it.toDto() }
	val tips = this.tips.map { it.toDto() }
	return RecipeDTO(
		this.id.toHexString(),
		this.name,
		this.description,
		this.language,
		this.user.id,
		ingredients,
		steps,
		tips,
		this.preparationDetails?.toDto() ?: PreparationDTO()
	)
}

fun Ingredient.toDto(): IngredientDTO {
	val quantity = this.quantity.toDto()
	val translations = this.localization.map { it.toDto() }
	return IngredientDTO(this.foodId.toHexString(), this.canonicalFoodName, translations, quantity, this.note)
}

fun Translation.toDto() = TranslationDTO(this.language, this.text)

fun Quantity.toDto() = QuantityDTO(this.value, this.unitOfMeasure)

fun Step.toDto() = StepDTO(this.number, this.description)

fun mapper(step: StepDTO): Step {
	return Step(step.number, step.description)
}

