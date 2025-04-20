package dev.bitvictory.aeon.exceptions

import dev.bitvictory.aeon.model.AeonError
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AuthenticationException(message: String, throwable: Throwable? = null): RuntimeException(message, throwable)
class AuthorizationException(message: String): RuntimeException(message)

@OptIn(ExperimentalUuidApi::class) fun StatusPagesConfig.authenticationStatusHandling() {
	exception<AuthenticationException> { call, cause ->
		val errorResponse = AeonError(
			correlationId = Uuid.random().toHexString(),
			message = cause.message ?: "The authentication failed"
		)
		call.respond(HttpStatusCode.Unauthorized, errorResponse)
	}
	exception<AuthorizationException> { call, cause ->
		val errorResponse = AeonError(
			correlationId = Uuid.random().toHexString(),
			message = cause.message ?: "The authorization failed"
		)
		call.respond(HttpStatusCode.Forbidden, errorResponse)
	}
}
