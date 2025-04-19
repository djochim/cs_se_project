package dev.bitvictory.aeon.client

import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.api.system.SystemHealthDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO

interface AeonApi {
	suspend fun getPrivacyInformation(): AeonResponse<PrivacyInformationDTO>

	suspend fun getStatus(): AeonResponse<SystemHealthDTO>
}