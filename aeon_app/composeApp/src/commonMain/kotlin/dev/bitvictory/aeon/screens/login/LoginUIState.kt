package dev.bitvictory.aeon.screens.login

import dev.bitvictory.aeon.screens.RootUIState

data class LoginUIState(
	val email: String = "",
	val password: String = "",
	val isLoading: Boolean = false,
	val success: Boolean = false,
	val error: String = "",
	val emailError: String = "",
	val passwordError: String = "",
): RootUIState(false)
