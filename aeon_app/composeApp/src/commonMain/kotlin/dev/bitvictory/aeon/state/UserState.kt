package dev.bitvictory.aeon.state

import dev.bitvictory.aeon.model.api.user.UserDTO
import dev.bitvictory.aeon.model.api.user.auth.TokenDTO

data class UserState(
	val token: TokenDTO? = null,
	val user: UserDTO? = null
) {
	val id: String
		get() = token?.userId ?: ""
	val refreshToken: String
		get() = token?.refreshToken ?: ""
	val accessToken: String
		get() = token?.accessToken ?: ""
	val name: String
		get() = user?.name ?: ""
	val email: String
		get() = user?.email ?: ""

	fun isAuthenticated() = token != null
}
