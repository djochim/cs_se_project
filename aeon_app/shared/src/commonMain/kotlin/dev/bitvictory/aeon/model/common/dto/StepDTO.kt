package dev.bitvictory.aeon.model.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class StepDTO(
    val number: Int,
    val description: String,
)
