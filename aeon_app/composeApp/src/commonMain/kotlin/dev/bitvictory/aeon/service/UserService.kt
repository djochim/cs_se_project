package dev.bitvictory.aeon.service

import dev.bitvictory.aeon.client.AuthClient
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.api.user.auth.LoginDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginRefreshDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import dev.bitvictory.aeon.state.UserState
import dev.bitvictory.aeon.storage.SharedSettingsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserService(
	private val authClient: AuthClient,
	private val sharedSettingsHelper: SharedSettingsHelper
) {

	private val _userState = MutableStateFlow(UserState())
	val userState: StateFlow<UserState> = _userState

	init {
		val tokenDTO = sharedSettingsHelper.token
		if (tokenDTO != null) {
			updateStateWithToken(tokenDTO)
		}
	}

	fun isAuthenticated() = userState.value.isAuthenticated()

	suspend fun login(loginDTO: LoginDTO): AeonResponse<TokenDTO> {
		val loginResult = authClient.login(loginDTO)
		if (loginResult is AeonSuccessResponse) {
			sharedSettingsHelper.token = loginResult.data
			updateStateWithToken(loginResult.data)
		}
		return loginResult
	}

	suspend fun refreshLogin(): AeonResponse<TokenDTO> {
		val loginResult = authClient.refreshLogin(LoginRefreshDTO(refreshToken = userState.value.refreshToken))
		if (loginResult is AeonSuccessResponse) {
			sharedSettingsHelper.token = loginResult.data
			updateStateWithToken(loginResult.data)
		}
		return loginResult
	}

	fun logout() {
		sharedSettingsHelper.token = null
		clearState()
	}

	private fun updateStateWithToken(tokenDTO: TokenDTO) {
		_userState.value = UserState(tokenDTO.userId, tokenDTO.accessToken, tokenDTO.refreshToken, "")
	}

	private fun clearState() {
		_userState.value = UserState()
	}

}