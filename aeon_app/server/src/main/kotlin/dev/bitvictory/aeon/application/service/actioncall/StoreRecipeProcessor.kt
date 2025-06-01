package dev.bitvictory.aeon.application.service.actioncall

import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonAction
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonFunctionCall
import dev.bitvictory.aeon.core.domain.entities.assistant.action.AeonToolOutput
import dev.bitvictory.aeon.core.domain.entities.assistant.action.StoreRecipeFunction
import dev.bitvictory.aeon.core.domain.entities.food.Food
import dev.bitvictory.aeon.core.domain.entities.food.FoodScore
import dev.bitvictory.aeon.core.domain.entities.food.Translation
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
		val ingredients = getIngredients(recipeFIO.ingredients, recipeFIO.language)
		return Recipe(
			ObjectId.get(),
			recipeFIO.name,
			recipeFIO.language,
			recipeFIO.description,
			user,
			ingredients,
			getSteps(recipeFIO.steps),
			recipeFIO.tips?.map { Tip(it) } ?: emptyList())
	}

	private fun getSteps(steps: List<StepFIO>): List<Step> = steps.map { Step(it.stepNumber, it.description) }

	private suspend fun getIngredients(ingredients: List<IngredientFIO>, language: String): List<Ingredient> {
		val persistedFood = foodPersistence.searchAll(ingredients.map { it.canonicalName }, language).filter { (it.value?.score ?: 0).toDouble() > 0.5 }
		val missingIngredients = ingredients.filter { !persistedFood.containsKey(it.canonicalName) }
		val missingTranslation = getFoodWithMissingTranslation(persistedFood, ingredients)
		val persistedFoodWithTranslation = missingTranslation.associate { it.first.first to it.first.second }
		val createdFoods = createMissingFoods(missingIngredients).associateBy { it.canonicalName }
		foodPersistence.addTranslations(missingTranslation.filter { it.second.isNotEmpty() }.associate { it.first.second.id to it.second })
		return ingredients.map {
			val food = (persistedFoodWithTranslation[it.canonicalName]?.toFood() ?: createdFoods[it.canonicalName])
				?: throw IllegalStateException("Unknown foods should have been created")
			Ingredient(food.id, food.canonicalName, it.parsedQuantity(), it.note, food.translations.filter { it.language == language })
		}
	}

	private fun getFoodWithMissingTranslation(
		persistedFood: Map<String, FoodScore?>,
		ingredients: List<IngredientFIO>
	): List<Pair<Pair<String, FoodScore>, List<Translation>>> {
		return ingredients.filter { persistedFood[it.canonicalName] != null }.map { ingredient ->
			val foodScore = persistedFood[ingredient.canonicalName]!!
			val availableTranslations = foodScore.translations.map { it.language }.toSet()
			val requiredTranslation = ingredient.localizations
			val newTranslation = requiredTranslation.filter { !availableTranslations.contains(it.lang) }.map { Translation(it.lang, it.name) }
			ingredient.canonicalName to foodScore.copy(translations = foodScore.translations + newTranslation) to newTranslation
		}
	}

	private suspend fun createMissingFoods(missingIngredients: List<IngredientFIO>): List<Food> {
		val missingFoods = missingIngredients.map { Food(ObjectId.get(), it.canonicalName, it.localizations.map { loc -> Translation(loc.lang, loc.name) }) }
		foodPersistence.insertAll(missingFoods)
		return missingFoods
	}

}