package dev.bitvictory.aeon.model.api.user.privacy

import kotlinx.serialization.Serializable

@Serializable
data class PrivacyInformationPatchDTO(
	val key: String,
	val deletions: List<PrivacyInformationKeyDTO>,
	val changes: List<PrivacyInformationEntryDTO>
)
