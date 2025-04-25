package dev.bitvictory.aeon.client.aeon

import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.api.system.SystemHealthDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationPatchDTO

interface AeonApi {
	suspend fun getPrivacyInformation(): AeonResponse<PrivacyInformationDTO>

	suspend fun patchPrivacyInformation(privacyInformationPatchDTO: PrivacyInformationPatchDTO): AeonResponse<Unit>

	suspend fun getStatus(): AeonResponse<SystemHealthDTO>
}