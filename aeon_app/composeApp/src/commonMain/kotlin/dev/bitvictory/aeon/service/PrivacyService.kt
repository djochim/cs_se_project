package dev.bitvictory.aeon.service

import dev.bitvictory.aeon.client.AeonApiClient
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO

class PrivacyService(
	private val aeonApiClient: AeonApiClient,
) {

	suspend fun getPrivacyInformation(): AeonResponse<PrivacyInformationDTO> = aeonApiClient.getPrivacyInformation()

}