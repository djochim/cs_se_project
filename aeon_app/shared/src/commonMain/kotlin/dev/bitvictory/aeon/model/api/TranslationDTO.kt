package dev.bitvictory.aeon.model.api

import kotlinx.serialization.Serializable

@Serializable
data class TranslationDTO(
	val language: String,
	val text: String?
)