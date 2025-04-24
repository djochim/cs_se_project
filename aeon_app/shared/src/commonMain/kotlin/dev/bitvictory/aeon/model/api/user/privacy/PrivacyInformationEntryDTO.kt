package dev.bitvictory.aeon.model.api.user.privacy

import kotlinx.serialization.Serializable

@Serializable
data class PrivacyInformationEntryDTO(
	val key: String,
	val value: String,
	val isDeletable: Boolean = true,
)
