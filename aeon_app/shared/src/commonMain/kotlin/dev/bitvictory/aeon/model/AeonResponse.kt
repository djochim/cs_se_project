package dev.bitvictory.aeon.model

sealed class AeonResponse<T>

data class AeonSuccessResponse<T>(val data: T): AeonResponse<T>()
data class AeonErrorResponse<T>(val statusCode: Int, val error: String, val type: ErrorType): AeonResponse<T>()
