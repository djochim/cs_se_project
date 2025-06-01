package dev.bitvictory.aeon.model.api

import kotlinx.serialization.Serializable

@Serializable
data class FoodDTO(
    val id: String,
    val name: String
)
