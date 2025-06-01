package dev.bitvictory.aeon.model.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class ImageRefDTO(
    val description: String,
    val source: String
)
