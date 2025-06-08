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

/**
 * Configures error handling for the application.
 *
 * This function installs the `StatusPages` feature and defines how different types of errors and exceptions
 * should be handled by the server. It includes specific handlers for authentication and conflict errors,
 * and a general handler for any other `Throwable`.
 *
 * The general exception handler logs the error and responds to the client with an `InternalServerError`
 * status code and a standardized `AeonError` JSON response containing a unique `correlationId` and
 * a generic error message.
 *
 * This function is annotated with `@OptIn(ExperimentalUuidApi::class)` because it uses the experimental
 * `Uuid.random().toHexString()` for generating correlation IDs.
 */
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