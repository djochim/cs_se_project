package dev.bitvictory.aeon.client

import dev.bitvictory.aeon.model.AeonResponse
import dev.bitvictory.aeon.model.api.user.UpdateUserRequest
import dev.bitvictory.aeon.model.api.user.UserDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginDTO
import dev.bitvictory.aeon.model.api.user.auth.LoginRefreshDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * Interface defining the API for Identity and Access Management (IAM) operations.
 * This interface provides methods for user authentication, token management, and user profile operations.
 */
@OptIn(ExperimentalSerializationApi::class)
interface IAMApi {
	val tokenRefreshEvents: SharedFlow<AeonResponse<TokenDTO>>

	/**
	 * Logs in a user.
	 *
	 * This function sends a login request to the server with the provided login credentials.
	 *
	 * @param login The [LoginDTO] object containing the user's login credentials (e.g., username and password).
	 * @return An [AeonResponse] object.
	 *         If the login is successful, the [AeonResponse] will contain a [TokenDTO] with the authentication token.
	 *         If the login fails (e.g., invalid credentials), the [AeonResponse] will indicate an error.
	 */
	suspend fun login(login: LoginDTO): AeonResponse<TokenDTO>

	/**
	 * Refreshes an existing login session using a refresh token.
	 *
	 * This function is typically used when an access token has expired.
	 * It sends a request to the authentication server with a valid refresh token
	 * to obtain a new access token and potentially a new refresh token.
	 *
	 * @param refresh A [LoginRefreshDTO] object containing the refresh token.
	 * @return An [AeonResponse] object that will eventually contain:
	 *         - On success: A [TokenDTO] object with the new access token and potentially a new refresh token.
	 *         - On failure: An error indicating why the refresh token was not accepted (e.g., expired, invalid).
	 */
	suspend fun refreshLogin(refresh: LoginRefreshDTO): AeonResponse<TokenDTO>

	suspend fun getUser(): AeonResponse<UserDTO>

	suspend fun updateUser(user: UpdateUserRequest): AeonResponse<Any>
}