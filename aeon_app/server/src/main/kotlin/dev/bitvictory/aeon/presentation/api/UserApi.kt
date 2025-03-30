package dev.bitvictory.aeon.presentation.api

import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationEntryDTO
import dev.bitvictory.aeon.model.api.user.privacy.PrivacyInformationGroupDTO
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.user() {
	authenticate {
		route("/user") {
			route("/privacy/information") {
				get {
					call.respond(
						HttpStatusCode.OK,
						PrivacyInformationDTO(listOf(PrivacyInformationGroupDTO("group", listOf(PrivacyInformationEntryDTO("key", "value")))))
					)
				}
			}
		}
	}
}
