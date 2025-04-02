package dev.bitvictory.aeon.screens.login

import dev.bitvictory.aeon.screens.RootUIState

data class LoginUIState(
	val email: String = "",
	val password: String = ""
): RootUIState(false)
