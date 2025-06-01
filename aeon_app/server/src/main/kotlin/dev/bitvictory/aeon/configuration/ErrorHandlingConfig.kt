package dev.bitvictory.aeon.configuration

import dev.bitvictory.aeon.exceptions.authenticationStatusHandling
import dev.bitvictory.aeon.exceptions.conflictStatusHandling
import dev.bitvictory.aeon.model.AeonError
import io.klogging.logger
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val log = logger("StatusPage")
@OptIn(ExperimentalUuidApi::class) fun Application.configureErrorHandling() {
	install(StatusPages) {
		authenticationStatusHandling()
		conflictStatusHandling()
		exception<Throwable> { call, cause ->
			val errorResponse = AeonError(
				correlationId = Uuid.random().toHexString(),
				message = "Internal Server error"
			)
			log.error(cause)
			call.respond(HttpStatusCode.InternalServerError, errorResponse)
		}
	}
}