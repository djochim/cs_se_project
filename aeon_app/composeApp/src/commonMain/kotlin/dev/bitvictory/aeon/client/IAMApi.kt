package dev.bitvictory.aeon.client

import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.api.user.UpdateUserRequest
import dev.bitvictory.aeon.model.api.user.UserDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginRefreshDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
interface IAMApi {
	val tokenRefreshEvents: SharedFlow<AeonResponse<TokenDTO>>

	suspend fun login(login: LoginDTO): AeonResponse<TokenDTO>

	suspend fun refreshLogin(refresh: LoginRefreshDTO): AeonResponse<TokenDTO>

	suspend fun getUser(): AeonResponse<UserDTO>

	suspend fun updateUser(user: UpdateUserRequest): AeonResponse<Any>
}