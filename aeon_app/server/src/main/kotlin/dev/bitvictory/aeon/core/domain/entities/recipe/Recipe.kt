package dev.bitvictory.aeon.core.domain.entities.recipe

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Recipe(
    @BsonId val id: ObjectId = ObjectId(),
    val name: String,
    val ingredients: List<Ingredient>,
    val steps: List<Step>,
    val tips: List<Tip>
)
