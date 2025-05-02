package dev.bitvictory.aeon.service

import dev.bitvictory.aeon.client.aeon.AeonApi
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.api.advisory.AdvisoryDTO
import dev.bitvictory.aeon.model.api.advisory.AdvisoryIdDTO
import dev.bitvictory.aeon.model.api.advisory.MessageDTO
import dev.bitvictory.aeon.model.api.advisory.request.AdvisoryMessageRequest

interface IAdvisorService {
	suspend fun initiateAdvisory(advisoryMessageRequest: AdvisoryMessageRequest): AeonResponse<AdvisoryDTO>

	suspend fun getAdvisory(advisoryIdDTO: AdvisoryIdDTO): AeonResponse<AdvisoryDTO>

	suspend fun addMessage(advisoryIdDTO: AdvisoryIdDTO, advisoryMessageRequest: AdvisoryMessageRequest): AeonResponse<MessageDTO>
}

class AdvisorService(
	private val aeonApi: AeonApi
): IAdvisorService {

	override suspend fun initiateAdvisory(advisoryMessageRequest: AdvisoryMessageRequest): AeonResponse<AdvisoryDTO> {
		return aeonApi.postAdvisory(advisoryMessageRequest)
	}

	override suspend fun getAdvisory(advisoryIdDTO: AdvisoryIdDTO): AeonResponse<AdvisoryDTO> {
		return aeonApi.getAdvisory(advisoryIdDTO)
	}

	override suspend fun addMessage(advisoryIdDTO: AdvisoryIdDTO, advisoryMessageRequest: AdvisoryMessageRequest): AeonResponse<MessageDTO> {
		return aeonApi.postMessage(id = advisoryIdDTO, advisoryMessageRequest = advisoryMessageRequest)
	}

}