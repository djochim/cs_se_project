package dev.bitvictory.aeon.infrastructure.database.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.TextSearchOptions
import dev.bitvictory.aeon.core.domain.entities.recipe.Recipe
import dev.bitvictory.aeon.core.domain.usecases.recipe.RecipePersistence
import dev.bitvictory.aeon.infrastructure.database.Database
import dev.bitvictory.aeon.model.primitive.Page
import io.ktor.util.logging.KtorSimpleLogger
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
	}

	private val collection = database.value.getCollection<Recipe>(COLLECTION_NAME)
	private val logger = KtorSimpleLogger(this.javaClass.name)

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

	suspend fun getAll(page: Page): List<Recipe> {
		logger.debug("Get Recipe page $page")
		return collection.find<Recipe>().sort(Sorts.ascending(Recipe::id.name)).limit(page.limit)
			.skip(page.offset).toList()
	}

	suspend fun getById(id: ObjectId): Recipe {
		logger.debug("Get Recipe by id $id")
		val filter = Filters.eq("_id", id)
		return collection.find<Recipe>(filter).first()
	}

	suspend fun delete(id: ObjectId): Long {
		logger.debug("Delete Recipe by id $id")
		val filter = Filters.eq("_id", id)
		return collection.deleteOne(filter).deletedCount
	}

	suspend fun search(searchQuery: String, page: Page): List<Recipe> {
		logger.debug("Search for Recipe with query $searchQuery page $page")
		val options = TextSearchOptions().caseSensitive(false).diacriticSensitive(false)
		val filter = Filters.text(searchQuery, options)
		return collection.find<Recipe>(filter).sort(Sorts.ascending(Recipe::id.name))
			.limit(page.limit).skip(page.offset).toList()
	}

}