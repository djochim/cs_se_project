package dev.bitvictory.aeon.core.domain.usecases.user

import dev.bitvictory.aeon.configuration.UserContext
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData

interface PersonaDataProvider {

	suspend fun getPersonalData(userContext: UserContext): PersonalData

}