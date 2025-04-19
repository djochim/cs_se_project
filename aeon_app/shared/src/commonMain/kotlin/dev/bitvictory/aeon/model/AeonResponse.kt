package dev.bitvictory.aeon.model

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
