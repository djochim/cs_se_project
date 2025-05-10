package dev.bitvictory.aeon.application.service

import dev.bitvictory.aeon.core.domain.entities.food.Food
import dev.bitvictory.aeon.core.domain.entities.recipe.Ingredient
import dev.bitvictory.aeon.core.domain.entities.recipe.Recipe
import dev.bitvictory.aeon.infrastructure.database.repository.RecipeCollection
import dev.bitvictory.aeon.model.primitive.Page
import org.bson.types.ObjectId
import java.time.Instant
import java.util.Date

val DEFAULT_INGREDIENT_ID: ObjectId = ObjectId.getSmallestWithDate(Date.from(Instant.EPOCH))

class RecipeService(
    private val recipeCollection: RecipeCollection,
    private val foodService: FoodService
) {

    suspend fun insert(recipe: Recipe) {
        val replacedIngredients = findBestFoods(recipe.ingredients)
        recipeCollection.insert(recipe.copy(ingredients = replacedIngredients))
    }

    suspend fun getAll(page: Page) = recipeCollection.getAll(page)

    suspend fun getById(id: ObjectId) = recipeCollection.getById(id)

    suspend fun delete(id: ObjectId) = recipeCollection.delete(id)

    suspend fun search(searchQuery: String, page: Page) = recipeCollection.search(searchQuery, page)

    private suspend fun findBestFoods(ingredients: List<Ingredient>): List<Ingredient> {
        return ingredients.map { findBestFood(it) }
    }

    private suspend fun findBestFood(ingredient: Ingredient): Ingredient {
        if (DEFAULT_INGREDIENT_ID != ingredient.foodId) {
            return ingredient
        }
        val bestFood = foodService.findBest(ingredient.foodName) ?: createNewFood(ingredient)
        return Ingredient(bestFood.id, bestFood.name, ingredient.quantity)
    }

    private suspend fun createNewFood(ingredient: Ingredient): Food {
        val food = Food(name = ingredient.foodName)
        foodService.insert(food)
        return food
    }

}