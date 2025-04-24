package dev.bitvictory.aeon.model.api.user.privacy

import kotlinx.serialization.Serializable

@Serializable
data class PrivacyInformationGroupDTO(
	val key: String,
	val name: String,
	val entries: List<PrivacyInformationEntryDTO>
)
