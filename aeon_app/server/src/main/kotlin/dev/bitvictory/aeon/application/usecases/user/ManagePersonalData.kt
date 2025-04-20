package dev.bitvictory.aeon.application.usecases.user

import dev.bitvictory.aeon.configuration.UserContext
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData

interface ManagePersonalData {

	suspend fun getPersonalData(userContext: UserContext): PersonalData

}