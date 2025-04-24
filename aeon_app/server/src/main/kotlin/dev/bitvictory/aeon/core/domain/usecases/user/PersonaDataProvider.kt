package dev.bitvictory.aeon.core.domain.usecases.user

import dev.bitvictory.aeon.configuration.UserContext
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryKey
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataChangeRequest

interface PersonaDataProvider {

	suspend fun getCategories(): List<PersonalDataCategoryKey>

	suspend fun getPersonalData(userContext: UserContext): PersonalData

	suspend fun patchPersonalData(userContext: UserContext, personalDataChangeRequest: PersonalDataChangeRequest)

}