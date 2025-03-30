package dev.bitvictory.aeon.model.api.user.privacy

import kotlinx.serialization.Serializable

@Serializable
data class PrivacyInformationDTO(
	val groups: List<PrivacyInformationGroupDTO>
)
