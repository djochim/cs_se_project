package dev.bitvictory.aeon.infrastructure.database.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.TextSearchOptions
import dev.bitvictory.aeon.core.domain.entities.food.Food
import dev.bitvictory.aeon.core.domain.entities.food.FoodScore
import dev.bitvictory.aeon.core.domain.usecases.food.FoodPersistence
import dev.bitvictory.aeon.infrastructure.database.Database
import dev.bitvictory.aeon.model.primitive.Page
import io.klogging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.bson.types.ObjectId

class FoodCollection(database: Database): FoodPersistence {

	companion object {
		const val COLLECTION_NAME = "foods"
	}

	private val collection = database.value.getCollection<Food>(COLLECTION_NAME)
	private val logger = logger(this.javaClass.name)

	init {
		val scope = CoroutineScope(Job() + Dispatchers.IO)
		scope.launch {
			collection.createIndex(Indexes.text(Food::name.name))
		}
	}

	override suspend fun insert(food: Food) {
		logger.debug("Insert food ${food.name}")
		collection.insertOne(food)
	}

	override suspend fun insertAll(foods: List<Food>) {
		logger.debug("Insert foods ${foods.map { it.name }}")
		if (foods.isEmpty()) return
		collection.insertMany(foods)
	}

	suspend fun getAll(page: Page): List<Food> {
		logger.debug("Get Food page $page")
		return collection.find<Food>().sort(Sorts.ascending(Food::id.name)).limit(page.limit)
			.skip(page.offset).toList()
	}

	suspend fun getById(id: ObjectId): Food {
		logger.debug("Get Food by id $id")
		val filter = Filters.eq("_id", id)
		return collection.find<Food>(filter).first()
	}

	suspend fun delete(id: ObjectId): Long {
		logger.debug("Delete Food by id $id")
		val filter = Filters.eq("_id", id)
		return collection.deleteOne(filter).deletedCount
	}

	suspend fun search(searchQuery: String, page: Page): List<Food> {
		logger.debug("Search for Food with query $searchQuery page $page")
		val options = TextSearchOptions().caseSensitive(false).diacriticSensitive(false)
		val filter = Filters.text(searchQuery, options)
		return collection.find<Food>(filter).sort(Sorts.ascending(Food::id.name)).limit(page.limit)
			.skip(page.offset).toList()
	}

	override suspend fun searchAll(searchQuery: List<String>): Map<String, FoodScore?> {
		if (searchQuery.isEmpty()) return emptyMap()
		return searchQuery.map { search(it) }.associate { it.await() }
	}

	private fun search(searchQuery: String): Deferred<Pair<String, FoodScore?>> {
		val options = TextSearchOptions().caseSensitive(false).diacriticSensitive(false)
		val projection = Projections.fields(
			Projections.include(Food::id.name),
			Projections.include(Food::name.name),
			Projections.metaTextScore("score")
		)
		val filter = Filters.text(searchQuery, options)
		return CoroutineScope(Dispatchers.IO).async {
			searchQuery to collection.find<FoodScore>(filter).projection(projection).sort(Sorts.descending(FoodScore::score.name)).limit(1).toList()
				.firstOrNull()
		}
	}

}