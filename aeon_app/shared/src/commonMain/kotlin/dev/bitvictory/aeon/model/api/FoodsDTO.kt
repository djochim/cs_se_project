package dev.bitvictory.aeon.model.api

import kotlinx.serialization.Serializable

@Serializable
data class FoodsDTO(
    val items: List<FoodDTO>
)
