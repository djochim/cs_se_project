package dev.bitvictory.aeon.infrastructure.database.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.TextSearchOptions
import dev.bitvictory.aeon.core.domain.entities.recipe.Recipe
import dev.bitvictory.aeon.core.domain.entities.recipe.RecipeHeader
import dev.bitvictory.aeon.core.domain.entities.user.User
import dev.bitvictory.aeon.core.domain.usecases.recipe.RecipePersistence
import dev.bitvictory.aeon.infrastructure.database.Database
import dev.bitvictory.aeon.model.primitive.Page
import io.klogging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.bson.types.ObjectId

class RecipeCollection(database: Database): RecipePersistence {

	companion object {
		const val COLLECTION_NAME = "recipes"
		private val RecipeHeaderProjection = Projections.fields(
			Projections.include(Recipe::id.name),
			Projections.include(Recipe::name.name),
			Projections.include(Recipe::language.name),
			Projections.include(Recipe::description.name),
			Projections.include(Recipe::user.name),
			Projections.include(Recipe::preparationDetails.name)
		)
	}

	private val collection = database.value.getCollection<Recipe>(COLLECTION_NAME)
	private val logger = logger<RecipeCollection>()

	init {
		val scope = CoroutineScope(Job() + Dispatchers.IO)
		scope.launch {
			collection.createIndex(Indexes.text(Recipe::name.name))
		}
	}

	override suspend fun insert(recipe: Recipe) {
		logger.debug("Insert food ${recipe.id}")
		collection.insertOne(recipe)
	}

	override suspend fun getAll(page: Page, user: User): List<RecipeHeader> {
		logger.debug("Get Recipe page $page")
		val filter = Filters.eq("${Recipe::user.name}.${User::id.name}", user.id)
		return collection.find<RecipeHeader>().projection(RecipeHeaderProjection).filter(filter).sort(Sorts.ascending(Recipe::id.name)).limit(page.limit)
			.skip(page.offset).toList()
	}

	override suspend fun getById(id: ObjectId, user: User): Recipe {
		logger.debug("Get Recipe by id $id")
		val filter = Filters.and(Filters.eq("_id", id), Filters.eq("${Recipe::user.name}.${User::id.name}", user.id))
		return collection.find<Recipe>(filter).first()
	}

	override suspend fun delete(id: ObjectId, user: User): Long {
		logger.debug("Delete Recipe by id $id")
		val filter = Filters.and(Filters.eq("_id", id), Filters.eq("${Recipe::user.name}.${User::id.name}", user.id))
		return collection.deleteOne(filter).deletedCount
	}

	override suspend fun search(searchQuery: String, page: Page, user: User): List<RecipeHeader> {
		logger.debug("Search for Recipe with query $searchQuery page $page")
		val options = TextSearchOptions().caseSensitive(false).diacriticSensitive(false)
		val filter = Filters.and(Filters.text(searchQuery, options), Filters.eq("${Recipe::user.name}.${User::id.name}", user.id))
		return collection.find<RecipeHeader>(filter).projection(RecipeHeaderProjection).sort(Sorts.ascending("score"))
			.limit(page.limit).skip(page.offset).toList()
	}

}