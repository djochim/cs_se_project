package dev.bitvictory.aeon.core.domain.entities.food

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Food(
    @BsonId val id: ObjectId = ObjectId(),
    val name: String
)
