package dev.bitvictory.aeon.service

import dev.bitvictory.aeon.client.AeonApi
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO

class PrivacyService(
	private val aeonApi: AeonApi,
) {

	suspend fun getPrivacyInformation(): AeonResponse<PrivacyInformationDTO> = aeonApi.getPrivacyInformation()

}