package dev.bitvictory.aeon.model

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed class AeonResponse<T>

@Serializable
data class AeonError @OptIn(ExperimentalUuidApi::class) constructor(
	val correlationId: String = Uuid.random().toHexString(),
	val message: String,
	val details: Map<String, String> = mapOf(),
)

data class AeonSuccessResponse<T>(val data: T): AeonResponse<T>()
data class AeonErrorResponse<T>(val statusCode: Int, val error: AeonError, val type: ErrorType): AeonResponse<T>()

suspend inline fun <reified T> HttpResponse.aeonBody(): AeonResponse<T> {
	if (this.status.isSuccess()) {
		return AeonSuccessResponse(this.body())
	}
	var _error: AeonError? = null
	try {
		_error = this.body<AeonError>()
	} catch (e: Exception) {
		e.printStackTrace()
	}
	val error = _error ?: AeonError(message = this.status.description)
	if (this.status.value in 500..599) {
		return AeonErrorResponse(this.status.value, error, ErrorType.SERVER_ERROR)
	}
	if (this.status.value in 400..499) {
		return AeonErrorResponse(this.status.value, error, ErrorType.CLIENT_ERROR)
	}
	return AeonErrorResponse(this.status.value, error, ErrorType.UNKNOWN_ERROR)
}