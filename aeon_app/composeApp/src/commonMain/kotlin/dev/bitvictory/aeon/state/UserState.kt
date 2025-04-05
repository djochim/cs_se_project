package dev.bitvictory.aeon.state

data class UserState(
	val userId: String = "",
	val token: String = "",
	val refreshToken: String = "",
	val name: String = ""
) {
	fun isAuthenticated() = userId.isNotEmpty() && token.isNotEmpty() && refreshToken.isNotEmpty()
}
