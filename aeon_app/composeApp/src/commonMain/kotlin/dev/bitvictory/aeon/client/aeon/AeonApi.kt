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
import dev.bitvictory.aeon.model.primitive.Page

interface AeonApi {

	suspend fun postAdvisory(advisoryMessageRequest: AdvisoryMessageRequest): AeonResponse<AdvisoryDTO>

	suspend fun getAdvisory(id: AdvisoryIdDTO): AeonResponse<AdvisoryDTO>

	suspend fun postMessage(id: AdvisoryIdDTO, advisoryMessageRequest: AdvisoryMessageRequest): AeonResponse<MessageDTO>

	suspend fun getPrivacyInformation(): AeonResponse<PrivacyInformationDTO>

	suspend fun patchPrivacyInformation(privacyInformationPatchDTO: PrivacyInformationPatchDTO): AeonResponse<Unit>

	suspend fun getStatus(): AeonResponse<SystemHealthDTO>
	suspend fun getRecipes(page: Page, searchQuery: String?): AeonResponse<RecipesDTO>
	suspend fun getRecipe(id: String): AeonResponse<RecipeDTO>
}