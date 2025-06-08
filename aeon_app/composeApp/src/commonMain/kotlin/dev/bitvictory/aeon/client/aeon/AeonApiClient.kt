package dev.bitvictory.aeon.client.aeon

import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.api.advisory.AdvisoryDTO
import dev.bitvictory.aeon.model.api.advisory.AdvisoryIdDTO
import dev.bitvictory.aeon.model.api.advisory.MessageDTO
import dev.bitvictory.aeon.model.api.advisory.request.AdvisoryMessageRequest
import dev.bitvictory.aeon.model.api.recipes.RecipeDTO
import dev.bitvictory.aeon.model.api.recipes.RecipesDTO
import dev.bitvictory.aeon.model.api.system.SystemHealthDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationPatchDTO
import dev.bitvictory.aeon.model.common.util.requestWrapper
import dev.bitvictory.aeon.model.primitive.Page
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AeonApiClient internal constructor(
	private val baseUrl: String,
	private val client: HttpClient
): AeonApi {

	override suspend fun postAdvisory(advisoryMessageRequest: AdvisoryMessageRequest): AeonResponse<AdvisoryDTO> = requestWrapper {
		client.post("$baseUrl/advisories") {
			setBody(advisoryMessageRequest)
		}
	}

	override suspend fun getAdvisory(id: AdvisoryIdDTO): AeonResponse<AdvisoryDTO> = requestWrapper {
		client.get("$baseUrl/advisories/${id.id}")
	}

	override suspend fun postMessage(id: AdvisoryIdDTO, advisoryMessageRequest: AdvisoryMessageRequest): AeonResponse<MessageDTO> = requestWrapper {
		client.post("$baseUrl/advisories/${id.id}/messages") {
			parameter("id", id.id)
			setBody(advisoryMessageRequest)
		}
	}

	override suspend fun getPrivacyInformation(): AeonResponse<PrivacyInformationDTO> = requestWrapper {
		client.get("$baseUrl/user/privacy/information")
	}

	override suspend fun patchPrivacyInformation(privacyInformationPatchDTO: PrivacyInformationPatchDTO): AeonResponse<Unit> = requestWrapper {
		client.patch("$baseUrl/user/privacy/information") {
			setBody(privacyInformationPatchDTO)
		}
	}

	override suspend fun getStatus(): AeonResponse<SystemHealthDTO> = requestWrapper {
		client.get("$baseUrl/system/health")
	}

	override suspend fun getRecipes(page: Page, searchQuery: String?): AeonResponse<RecipesDTO> = requestWrapper {
		client.get("$baseUrl/recipes") {
			parameter("offset", page.offset)
			parameter("limit", page.limit)
			if (searchQuery != null) {
				parameter("search", searchQuery)
			}
		}
	}

	override suspend fun getRecipe(id: String): AeonResponse<RecipeDTO> = requestWrapper {
		client.get("$baseUrl/recipes/$id")
	}

}