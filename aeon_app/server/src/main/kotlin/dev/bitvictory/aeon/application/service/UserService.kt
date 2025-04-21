package dev.bitvictory.aeon.application.service

import dev.bitvictory.aeon.application.usecases.user.ManagePersonalData
import dev.bitvictory.aeon.configuration.UserContext
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData
import dev.bitvictory.aeon.core.domain.usecases.user.PersonaDataProvider

class UserService(private val personalDataProvider: List<PersonaDataProvider>): ManagePersonalData {

	override suspend fun getPersonalData(userContext: UserContext): PersonalData {
		val categories = personalDataProvider.map { it.getPersonalData(userContext) }.flatMap { it.categories }
		return PersonalData(categories)
	}

}