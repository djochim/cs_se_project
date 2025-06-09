package dev.bitvictory.aeon.model

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a sealed class for handling API responses.
 *
 * This class provides a structured way to represent different states of an API response,
 * including success, error, and loading states.
 *
 * @param T The type of data expected in the response.
 */
sealed class AeonResponse<T>

@Serializable
data class AeonError @OptIn(ExperimentalUuidApi::class) constructor(
	val correlationId: String = Uuid.random().toHexString(),
	val message: String,
	val details: Map<String, String> = mapOf(),
)

data class AeonSuccessResponse<T>(val data: T): AeonResponse<T>()
data class AeonErrorResponse<T>(val statusCode: Int, val error: AeonError, val type: ErrorType): AeonResponse<T>()

/**
 * Processes an HTTP response and converts it into an AeonResponse.
 *
 * This function checks the HTTP status code of the response.
 * - If the status code indicates success (2xx), it attempts to parse the response body as the specified type `T`
 *   and returns an [AeonSuccessResponse] containing the parsed body.
 * - If the status code indicates an error (4xx or 5xx), it attempts to parse the response body as an [AeonError].
 *   - If parsing the error body is successful, it uses that [AeonError].
 *   - If parsing the error body fails (e.g., the body is not a valid JSON for [AeonError] or is empty),
 *     it creates a default [AeonError] using the HTTP status description.
 *   It then returns an [AeonErrorResponse] with the HTTP status code, the determined [AeonError], and an
 *   appropriate [ErrorType] (CLIENT_ERROR for 4xx, SERVER_ERROR for 5xx).
 * - For any other status codes, it follows the error handling logic (attempts to parse [AeonError], falls back to default)
 *   and returns an [AeonErrorResponse] with [ErrorType.UNKNOWN_ERROR].
 *
 * This function is an inline suspend function, allowing for efficient asynchronous operations and reified type parameters.
 *
 * @param T The expected type of the successful response body. This type parameter is reified, meaning its
 *          type information is available at runtime, allowing for direct deserialization to `T`.
 * @return An [AeonResponse] which can be either an [AeonSuccessResponse] containing the body of type `T`,
 *         or an [AeonErrorResponse] containing details about the error.
 * @throws Exception if there's an issue deserializing the successful response body to type `T` when the status is success.
 *                   Note that errors during deserialization of the error body are caught and handled internally
 *                   by creating a default [AeonError].
 *
 * @see AeonResponse
 * @see AeonSuccessResponse
 * @see AeonErrorResponse
 * @see AeonError
 * @see ErrorType
 */
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