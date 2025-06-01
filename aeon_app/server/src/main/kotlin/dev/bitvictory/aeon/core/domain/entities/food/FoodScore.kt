package dev.bitvictory.aeon.core.domain.entities.food

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class FoodScore(
	@BsonId val id: ObjectId = ObjectId(),
	val canonicalName: String,
	val translations: List<Translation>,
	val score: Double
) {
	fun toFood() = Food(id, canonicalName, translations)
}
