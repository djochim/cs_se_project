package dev.bitvictory.aeon.model.common.util

import dev.bitvictory.aeon.model.AeonError
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.ErrorType
import dev.bitvictory.aeon.model.aeonBody
import io.ktor.client.statement.HttpResponse
import kotlinx.io.IOException

/**
 * Wraps a network request and handles potential exceptions.
 *
 * This function executes the provided `request` lambda, which is expected to return an `HttpResponse`.
 * If the request is successful, it attempts to parse the response body into an `AeonResponse<T>`.
 * If an `IOException` occurs during the request (e.g., network connectivity issues),
 * it catches the exception, prints the stack trace, and returns an `AeonErrorResponse`
 * indicating a server unavailability error.
 *
 * @param T The expected type of the successful response body. The function uses `reified`
 *          so that the type `T` is known at runtime, allowing for proper deserialization.
 * @param request A suspend lambda function that executes the network request and returns an `HttpResponse`.
 * @return An `AeonResponse<T>` which can be either a successful response containing the deserialized body
 *         of type `T`, or an `AeonErrorResponse` if an error occurred.
 *
 * @throws IOException This function catches `IOException` internally and converts it into an `AeonErrorResponse`.
 *                     However, other unexpected exceptions from the `request` lambda might still propagate.
 */
suspend inline fun <reified T> requestWrapper(request: () -> HttpResponse): AeonResponse<T> {
	try {
		val response = request()
		return response.aeonBody()
	} catch (e: IOException) {
		e.printStackTrace()
		return AeonErrorResponse(500, AeonError(message = "Error connecting to the server"), ErrorType.UNAVAILABLE_SERVER)
	}
}