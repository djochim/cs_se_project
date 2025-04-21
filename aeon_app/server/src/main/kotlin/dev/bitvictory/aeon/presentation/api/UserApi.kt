package dev.bitvictory.aeon.presentation.api

import dev.bitvictory.aeon.application.usecases.user.ManagePersonalData
import dev.bitvictory.aeon.configuration.userContext
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategory
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntry
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationEntryDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationGroupDTO
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.user() {
	authenticate {
		route("/user") {
			route("/privacy/information") {
				val managePersonalData: ManagePersonalData by inject()
				get {
					val personalData = managePersonalData.getPersonalData(call.userContext())
					call.respond(
						HttpStatusCode.OK,
						personalData.toDTO()
					)
				}
			}
		}
	}
}

fun PersonalData.toDTO() = PrivacyInformationDTO(this.categories.map { it.toDTO() })

fun PersonalDataCategory.toDTO() = PrivacyInformationGroupDTO(this.name.s, this.entries.map { it.toDTO() })

fun PersonalDataCategoryEntry.toDTO() = PrivacyInformationEntryDTO(this.name.s, this.value.s)
