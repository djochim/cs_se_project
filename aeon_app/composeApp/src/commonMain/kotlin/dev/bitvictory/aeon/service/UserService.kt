package dev.bitvictory.aeon.service

import dev.bitvictory.aeon.client.IAMApi
import dev.bitvictory.aeon.model.AeonErrorResponse
import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.AeonSuccessResponse
import dev.bitvictory.aeon.model.api.user.UserDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginRefreshDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import dev.bitvictory.aeon.state.UserState
import dev.bitvictory.aeon.storage.LocalKeyValueStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining the contract for user-related operations.
 *
 * This interface provides methods for managing user authentication,
 * retrieving user information, and observing user state.
 */
interface IUserService {
	val userState: StateFlow<UserState>
	fun isAuthenticated(): Boolean

	suspend fun login(loginDTO: LoginDTO): AeonResponse<TokenDTO>

	suspend fun refreshLogin(): AeonResponse<TokenDTO>

	suspend fun logout()

	suspend fun getUser(): AeonResponse<UserDTO>
}

class UserService(
	private val iamApi: IAMApi,
	private val localKeyValueStore: LocalKeyValueStore
): IUserService {

	private val _userState = MutableStateFlow(UserState())
	override val userState: StateFlow<UserState> = _userState

	init {
		val tokenDTO = localKeyValueStore.token
		val userDTO = localKeyValueStore.user
		if (tokenDTO != null) {
			_userState.value = _userState.value.copy(token = tokenDTO, user = userDTO)
		}
	}

	override fun isAuthenticated() = userState.value.isAuthenticated()

	override suspend fun login(loginDTO: LoginDTO): AeonResponse<TokenDTO> {
		val loginResult = iamApi.login(loginDTO)
		if (loginResult is AeonSuccessResponse) {
			localKeyValueStore.token = loginResult.data
			updateStateWithToken(loginResult.data)
			getUser()
		}
		return loginResult
	}

	override suspend fun refreshLogin(): AeonResponse<TokenDTO> {
		val loginResult = iamApi.refreshLogin(LoginRefreshDTO(refreshToken = userState.value.refreshToken))
		if (loginResult is AeonSuccessResponse) {
			localKeyValueStore.token = loginResult.data
			updateStateWithToken(loginResult.data)
		} else {
			logout()
		}
		return loginResult
	}

	override suspend fun logout() {
		localKeyValueStore.token = null
		localKeyValueStore.user = null
		clearState()
	}

	override suspend fun getUser(): AeonResponse<UserDTO> {
		return if (localKeyValueStore.user != null) {
			AeonSuccessResponse(localKeyValueStore.user!!)
		} else {
			val userResult = iamApi.getUser()
			if (userResult is AeonSuccessResponse) {
				localKeyValueStore.user = userResult.data
				updateStateWithUser(userResult.data)
			} else if (userResult is AeonErrorResponse && userResult.statusCode == 401) {
				logout()
			}
			userResult
		}
	}

	private suspend fun updateStateWithToken(tokenDTO: TokenDTO) {
		_userState.value = _userState.value.copy(token = tokenDTO)
	}

	private suspend fun updateStateWithUser(userDTO: UserDTO) {
		_userState.value = _userState.value.copy(user = userDTO)
	}

	private suspend fun clearState() {
		_userState.value = UserState()
	}

}