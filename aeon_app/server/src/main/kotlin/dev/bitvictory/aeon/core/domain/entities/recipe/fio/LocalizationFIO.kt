package dev.bitvictory.aeon.core.domain.entities.recipe.fio

import kotlinx.serialization.Serializable

@Serializable
data class LocalizationFIO(
	val lang: String,
	val name: String
)
