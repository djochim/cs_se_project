package dev.bitvictory.aeon.exceptions

import dev.bitvictory.aeon.model.AeonError
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ConflictException(message: String, val table: String = "unknown"): RuntimeException(message)

@OptIn(ExperimentalUuidApi::class) fun StatusPagesConfig.conflictStatusHandling() {
	exception<ConflictException> { call, cause ->
		val errorResponse = AeonError(
			correlationId = Uuid.random().toHexString(),
			message = "Your request conflicts with existing entities",
			details = mapOf("table" to cause.table, "message" to (cause.message ?: ""))
		)
		call.respond(HttpStatusCode.Conflict, errorResponse)
	}
}
