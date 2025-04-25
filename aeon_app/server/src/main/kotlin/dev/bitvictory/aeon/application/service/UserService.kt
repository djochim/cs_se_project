package dev.bitvictory.aeon.application.service

import dev.bitvictory.aeon.application.usecases.user.ManagePersonalData
import dev.bitvictory.aeon.configuration.UserContext
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataChangeRequest
import dev.bitvictory.aeon.core.domain.usecases.user.PersonaDataProvider

class UserService(private val personalDataProvider: List<PersonaDataProvider>): ManagePersonalData {

	override suspend fun getPersonalData(userContext: UserContext): PersonalData {
		val categories = personalDataProvider.map { it.getPersonalData(userContext) }.flatMap { it.categories }
		return PersonalData(categories)
	}

	override suspend fun patchPersonalData(userContext: UserContext, personalDataChangeRequest: PersonalDataChangeRequest) {
		val filteredProviders = personalDataProvider.filter { it.getCategories().contains(personalDataChangeRequest.key) }
		if (filteredProviders.isEmpty()) {
			throw Exception("No provider found for key ${personalDataChangeRequest.key}")
		}
		filteredProviders.forEach {
			it.patchPersonalData(userContext, personalDataChangeRequest)
		}
	}

}