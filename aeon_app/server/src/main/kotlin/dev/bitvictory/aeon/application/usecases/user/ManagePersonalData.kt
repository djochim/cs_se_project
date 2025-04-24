package dev.bitvictory.aeon.application.usecases.user

import dev.bitvictory.aeon.configuration.UserContext
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataChangeRequest

interface ManagePersonalData {

	suspend fun getPersonalData(userContext: UserContext): PersonalData
	
	suspend fun patchPersonalData(userContext: UserContext, personalDataChangeRequest: PersonalDataChangeRequest)

}