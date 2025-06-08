package dev.bitvictory.aeon.service

import dev.bitvictory.aeon.client.aeon.AeonApi
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationKeyDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationPatchDTO

/**
 * Interface defining operations related to user privacy and data management.
 * This service allows for retrieving privacy-related information and deleting specific data entries.
 */
interface IPrivacyService {
	suspend fun getPrivacyInformation(): AeonResponse<PrivacyInformationDTO>
	suspend fun deletePrivacyInformation(groupKey: String, entryKey: String): AeonResponse<Unit>
}

class PrivacyService(
	private val aeonApi: AeonApi,
): IPrivacyService {

	override suspend fun getPrivacyInformation(): AeonResponse<PrivacyInformationDTO> = aeonApi.getPrivacyInformation()

	override suspend fun deletePrivacyInformation(groupKey: String, entryKey: String): AeonResponse<Unit> {
		val privacyInformationPatchDTO = PrivacyInformationPatchDTO(groupKey, listOf(PrivacyInformationKeyDTO(entryKey)), emptyList())
		return aeonApi.patchPrivacyInformation(privacyInformationPatchDTO)
	}

}