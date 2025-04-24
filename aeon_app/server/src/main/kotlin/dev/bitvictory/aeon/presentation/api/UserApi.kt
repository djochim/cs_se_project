package dev.bitvictory.aeon.presentation.api

import dev.bitvictory.aeon.application.usecases.user.ManagePersonalData
import dev.bitvictory.aeon.configuration.userContext
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalData
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategory
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntry
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntryName
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryEntryValue
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataCategoryKey
import dev.bitvictory.aeon.core.domain.entities.user.personaldata.PersonalDataChangeRequest
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationEntryDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationGroupDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationKeyDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationPatchDTO
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
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
				patch {
					val personalDataChangeRequest = call.receive<PrivacyInformationPatchDTO>()
					managePersonalData.patchPersonalData(call.userContext(), personalDataChangeRequest.toDomain())
					call.respond(HttpStatusCode.OK)
				}
			}
		}
	}
}

fun PersonalData.toDTO() = PrivacyInformationDTO(this.categories.map { it.toDTO() })

fun PersonalDataCategory.toDTO() = PrivacyInformationGroupDTO(this.key.s, this.name.s, this.entries.map { it.toDTO() })

fun PersonalDataCategoryEntry.toDTO() = PrivacyInformationEntryDTO(this.name.s, this.value.s)

fun PrivacyInformationPatchDTO.toDomain() =
	PersonalDataChangeRequest(PersonalDataCategoryKey(this.key), this.deletions.map { it.toDomain() }, this.changes.map { it.toDomain() })

fun PrivacyInformationKeyDTO.toDomain() = PersonalDataCategoryEntryName(this.key)

fun PrivacyInformationEntryDTO.toDomain() = PersonalDataCategoryEntry(PersonalDataCategoryEntryName(this.key), PersonalDataCategoryEntryValue(this.value))
