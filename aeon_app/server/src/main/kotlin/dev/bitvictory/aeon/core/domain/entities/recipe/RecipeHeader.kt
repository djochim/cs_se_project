package dev.bitvictory.aeon.core.domain.entities.recipe

import dev.bitvictory.aeon.core.domain.entities.user.User
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class RecipeHeader(
	@BsonId val id: ObjectId = ObjectId(),
	val name: String,
	val language: String,
	val description: String,
	val user: User,
	val preparationDetails: PreparationDetails? = null
)
