package dev.bitvictory.aeon.application.service.actioncall

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonAction
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonFunctionCall
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutput
import dev.bitvictory.aeon.core.domain.entities.assistant.action.StoreRecipeFunction
import dev.bitvictory.aeon.core.domain.entities.food.Food
import dev.bitvictory.aeon.core.domain.entities.recipe.Ingredient
import dev.bitvictory.aeon.core.domain.entities.recipe.Recipe
import dev.bitvictory.aeon.core.domain.entities.recipe.Step
import dev.bitvictory.aeon.core.domain.entities.recipe.Tip
import dev.bitvictory.aeon.core.domain.entities.recipe.fio.IngredientFIO
import dev.bitvictory.aeon.core.domain.entities.recipe.fio.RecipeFIO
import dev.bitvictory.aeon.core.domain.entities.recipe.fio.StepFIO
import dev.bitvictory.aeon.core.domain.entities.recipe.mapper.toDto
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.core.domain.usecases.food.FoodPersistence
import dev.bitvictory.aeon.core.domain.usecases.recipe.RecipePersistence
import org.bson.types.ObjectId

class StoreRecipeProcessor(private val recipePersistence: RecipePersistence, private val foodPersistence: FoodPersistence): AeonActionProcessor {

	override fun actionName(): String = StoreRecipeFunction.name

	override suspend fun process(advisoryId: ObjectId, user: User, threadId: String, runId: String, aeonAction: AeonAction): AeonToolOutput {
		require(aeonAction is AeonFunctionCall)
		require(aeonAction.name == StoreRecipeFunction.name)
		val recipe = getRecipe(aeonAction, user)
		recipePersistence.insert(recipe)
		val recipeDto = recipe.toDto(User.CURRENT_PLACEHOLDER)
		return AeonToolOutput(aeonAction.callId, "Recipe ${recipeDto.name} was stored successfully")
	}

	private suspend fun getRecipe(functionCall: AeonFunctionCall, user: User): Recipe {
		val recipeFIO = FunctionInterfaceJson.decodeFromString(RecipeFIO.serializer(), functionCall.arguments)
		val ingredients = getIngredients(recipeFIO.ingredients)
		return Recipe(ObjectId.get(), recipeFIO.name, user, ingredients, getSteps(recipeFIO.steps), recipeFIO.tips?.map { Tip(it) } ?: emptyList())
	}

	private fun getSteps(steps: List<StepFIO>): List<Step> = steps.map { Step(it.stepNumber, it.description) }

	private suspend fun getIngredients(ingredients: List<IngredientFIO>): List<Ingredient> {
		val persistedFood = foodPersistence.searchAll(ingredients.map { it.name }).filter { (it.value?.score ?: 0).toDouble() > 0.5 }
		val missingIngredients = ingredients.filter { !persistedFood.containsKey(it.name) }
		val createdFoods = createMissingFoods(missingIngredients).associateBy { it.name }
		return ingredients.map {
			val food = (persistedFood[it.name]?.toFood() ?: createdFoods[it.name]) ?: throw IllegalStateException("Unknown foods should have been created")
			Ingredient(food.id, food.name, it.parsedQuantity(), it.note)
		}
	}

	private suspend fun createMissingFoods(missingIngredients: List<IngredientFIO>): List<Food> {
		val missingFoods = missingIngredients.map { Food(ObjectId.get(), it.name) }
		foodPersistence.insertAll(missingFoods)
		return missingFoods
	}

}