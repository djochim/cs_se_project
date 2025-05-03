package dev.bitvictory.aeon.client.aeon

import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.aeonBody
import dev.bitvictory.aeon.model.api.advisory.AdvisoryDTO
import dev.bitvictory.aeon.model.api.advisory.AdvisoryIdDTO
import dev.bitvictory.aeon.model.api.advisory.MessageDTO
import dev.bitvictory.aeon.model.api.advisory.request.AdvisoryMessageRequest
import dev.bitvictory.aeon.model.api.system.SystemHealthDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationPatchDTO
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.io.IOException

class AeonApiClient internal constructor(
	private val baseUrl: String,
	private val client: HttpClient
): AeonApi {

	override suspend fun postAdvisory(advisoryMessageRequest: AdvisoryMessageRequest): AeonResponse<AdvisoryDTO> {
		try {
			val response = client.post("$baseUrl/advisories") {
				setBody(advisoryMessageRequest)
			}
			return response.aeonBody()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, AeonError(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

	override suspend fun getAdvisory(id: AdvisoryIdDTO): AeonResponse<AdvisoryDTO> {
		try {
			val response = client.get("$baseUrl/advisories/${id.id}")
			return response.aeonBody()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, AeonError(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

	override suspend fun postMessage(id: AdvisoryIdDTO, advisoryMessageRequest: AdvisoryMessageRequest): AeonResponse<MessageDTO> {
		try {
			val response = client.post("$baseUrl/advisories/${id.id}/messages") {
				parameter("id", id.id)
				setBody(advisoryMessageRequest)
			}
			return response.aeonBody()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, AeonError(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

	override suspend fun getPrivacyInformation(): AeonResponse<PrivacyInformationDTO> {
		try {
			val response = client.get("$baseUrl/user/privacy/information")
			return response.aeonBody()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, AeonError(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

	override suspend fun patchPrivacyInformation(privacyInformationPatchDTO: PrivacyInformationPatchDTO): AeonResponse<Unit> {
		try {
			val response = client.patch("$baseUrl/user/privacy/information") {
				setBody(privacyInformationPatchDTO)
			}
			return response.aeonBody()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, AeonError(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

	override suspend fun getStatus(): AeonResponse<SystemHealthDTO> {
		try {
			val response = client.get("$baseUrl/system/health")
			return response.aeonBody()
		} catch (e: IOException) {
			e.printStackTrace()
			return AeonErrorResponse(500, AeonError(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
		}
	}

}