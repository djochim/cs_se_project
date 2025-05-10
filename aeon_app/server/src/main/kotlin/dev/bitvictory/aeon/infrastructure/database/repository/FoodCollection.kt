package dev.bitvictory.aeon.infrastructure.database.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.TextSearchOptions
import dev.bitvictory.aeon.core.domain.entities.food.Food
import dev.bitvictory.aeon.core.domain.entities.food.FoodScore
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

class FoodCollection(database: Database) {

    companion object {
        const val COLLECTION_NAME = "foods"
    }

    private val collection = database.value.getCollection<Food>(COLLECTION_NAME)
    private val logger = KtorSimpleLogger(this.javaClass.name)

    init {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            collection.createIndex(Indexes.text(Food::name.name))
        }
    }

    suspend fun insert(food: Food) {
        logger.debug("Insert food ${food.id}")
        collection.insertOne(food)
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

    suspend fun findSimilar(searchQuery: String, page: Page): List<FoodScore> {
        logger.debug("Find similar Food with query $searchQuery page")
        val metaTextScoreSort = Sorts.orderBy(
            Sorts.metaTextScore(FoodScore::score.name),
            Sorts.descending("_id")
        )
        val metaTextScoreProj = Projections.metaTextScore(FoodScore::score.name)
        val filter = Filters.text(searchQuery)
        return collection.find<FoodScore>(filter).projection(metaTextScoreProj)
            .sort(metaTextScoreSort).limit(page.limit).skip(page.offset).toList()
    }

}