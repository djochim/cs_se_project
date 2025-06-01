package dev.bitvictory.aeon.model.api.recipes

import dev.bitvictory.aeon.model.common.dto.ImageRefDTO
import kotlinx.serialization.Serializable

@Serializable
data class RecipeCreationDTO(
    val recipe: String,
    val images: List<ImageRefDTO>
)
