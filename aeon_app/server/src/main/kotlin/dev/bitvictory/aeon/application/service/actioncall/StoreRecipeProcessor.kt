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

/**
 * Processes actions related to storing recipes.
 *
 * This class is responsible for handling the "StoreRecipe" action, which involves:
 * 1. Parsing the recipe details from the action.
 * 2. Retrieving or creating food items for the ingredients.
 * 3. Persisting the recipe to the database.
 * 4. Returning a confirmation message.
 *
 * @property recipePersistence The persistence layer for recipes.
 * @property foodPersistence The persistence layer for food items.
 */
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

	/**
	 * Retrieves or creates ingredients based on the provided list of [IngredientFIO] objects and language.
	 *
	 * This function performs the following steps:
	 * 1. Searches for existing foods in the persistence layer based on the canonical names and language.
	 * 2. Identifies ingredients that are missing from the persistence layer.
	 * 3. Identifies existing foods that are missing translations for the specified language.
	 * 4. Creates new food entries for the missing ingredients.
	 * 5. Adds missing translations to existing food entries.
	 * 6. Constructs and returns a list of [Ingredient] objects, ensuring each ingredient has a corresponding food ID,
	 *    canonical name, parsed quantity, note, and relevant translations.
	 *
	 * @param ingredients A list of [IngredientFIO] objects representing the ingredients to process.
	 *                    [IngredientFIO] likely contains the canonical name and raw quantity/note information.
	 * @param language The target language for translations.
	 * @return A list of [Ingredient] objects, where each object represents a processed ingredient
	 *         with its associated food information and translations.
	 * @throws IllegalStateException if, after attempting to create missing foods, an unknown food is encountered.
	 *                               This indicates an unexpected state where a food that should have been created was not.
	 */
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