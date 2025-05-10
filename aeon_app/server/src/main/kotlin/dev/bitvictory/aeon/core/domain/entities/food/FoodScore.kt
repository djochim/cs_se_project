package dev.bitvictory.aeon.core.domain.entities.food

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class FoodScore(
    @BsonId val id: ObjectId = ObjectId(),
    val name: String,
    val score: Double
) {
    fun toFood() = Food(id, name)
}
