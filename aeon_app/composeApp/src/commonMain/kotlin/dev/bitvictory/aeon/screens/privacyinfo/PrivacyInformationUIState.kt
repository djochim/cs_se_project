package dev.bitvictory.aeon.screens.privacyinfo

import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationGroupDTO

data class PrivacyInformationUIState(
	val privacyInformation: PrivacyInformationDTO? = null,
	val error: AeonError? = null
) {
	val groups: List<PrivacyInformationGroupDTO>
		get() = privacyInformation?.groups ?: listOf()
}
